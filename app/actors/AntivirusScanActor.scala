package actors

import actors.antivirus.AntivirusScanExecution
import actors.antivirus.AntivirusScanExitCode
import actors.antivirus.AntivirusScanExitCode._
import config.SignalConsoConfiguration
import controllers.Logs.RichLogger
import models.FileData
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.DispatcherSelector
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import play.api.Logger
import repositories.FileDataRepositoryInterface
import service.S3ServiceInterface

import java.io.File
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.sys.process._
import scala.util.Failure
import scala.util.Success
object AntivirusScanActor {
  sealed trait ScanCommand
  final case class ScanFromFile(reportFile: FileData, file: java.io.File) extends ScanCommand
  final case object ScanFromFileSuccess                                   extends ScanCommand
  final case object ScanFromFileFailed                                    extends ScanCommand
  final case class ScanFromBucket(reportFile: FileData)                   extends ScanCommand
  final case class ScanFromBucketFailed(reportFile: FileData)             extends ScanCommand

  val logger: Logger = Logger(this.getClass)

  private def performAntivirusScan(file: java.io.File)(implicit ec: ExecutionContext): Future[AntivirusScanExecution] =
    Future {
      val stdout = new StringBuilder()
      val exitCode = Seq("clamdscan", "--remove", "--fdpass", file.toString) ! ProcessLogger(fn = s => {
        stdout.append(s)
        ()
      })
      AntivirusScanExecution(AntivirusScanExitCode.withValue(exitCode), stdout.toString())
    }

  def create(
      uploadConfiguration: SignalConsoConfiguration,
      reportFileRepository: FileDataRepositoryInterface,
      s3Service: S3ServiceInterface
  ): Behavior[ScanCommand] =
    Behaviors.setup { context =>
      implicit val ec: ExecutionContext =
        context.system.dispatchers.lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"))

      Behaviors.receiveMessage[ScanCommand] {
        case ScanFromBucket(reportFile: FileData) =>
          logger.warnWithTitle(
            "scan_rescanning_file",
            s"Rescanning file ${reportFile.id.value} : ${reportFile.storageFilename}"
          )
          val filePath = s"${uploadConfiguration.tmpDirectory}/${reportFile.filename}"
          context.pipeToSelf(s3Service.downloadOnCurrentHost(reportFile.storageFilename, filePath)) {
            case Success(_) =>
              val file = new File(filePath)
              ScanFromFile(reportFile, file)
            case Failure(_) =>
              ScanFromBucketFailed(reportFile)
          }
          Behaviors.same

        case ScanFromBucketFailed(reportFile: FileData) =>
          logger.warnWithTitle(
            "scan_rescanning_file_failed",
            s"failed to scan from bucket ${reportFile.storageFilename}"
          )
          Behaviors.same

        case ScanFromFile(reportFile: FileData, file: java.io.File) =>
          val filePath = s"${uploadConfiguration.tmpDirectory}/${reportFile.filename}"
          val result = for {
            existingFile <- {
              if (file.exists()) {
                Future.successful(file)
              } else {
                s3Service
                  .downloadOnCurrentHost(reportFile.storageFilename, filePath)
                  .map(_ => new File(filePath))
              }
            }
            _ = logger.debug("Begin Antivirus scan.")
            antivirusScanResult <- performAntivirusScan(existingFile)
            _ = logger.debug(
              s"Saving output to database : ${antivirusScanResult.output}"
            )
            _ <- reportFileRepository.setAvOutput(reportFile.id, antivirusScanResult.output)
            _ <- antivirusScanResult.exitCode match {
              case Some(NoVirusFound) | None =>
                logger.debug("Deleting file.")
                Future.successful(existingFile.delete())
              case Some(VirusFound) =>
                logger.warnWithTitle(
                  "scan_found_virus",
                  s"Antivirus scan found virus, scan output : ${antivirusScanResult.output}"
                )
                logger.debug(s"File has been deleted by Antivirus, removing file from S3")
                s3Service
                  .delete(reportFile.storageFilename)
                  .map(_ => reportFileRepository.removeStorageFileName(reportFile.id))
              case Some(ErrorOccured) =>
                logger.errorWithTitle(
                  "scan_unexpected_error",
                  s"Unexpected error occured when running scan on file $filePath : ${antivirusScanResult.output}"
                )
                Future.successful(Done)
            }
          } yield Done

          context.pipeToSelf(result) {
            case Success(_) => ScanFromFileSuccess
            case Failure(_) => ScanFromFileFailed
          }

          Behaviors.same

        case ScanFromFileSuccess =>
          logger.debug("Scan from file succeeded")
          Behaviors.same
        case ScanFromFileFailed =>
          logger.warnWithTitle("scan_from_file_failed", s"Scan from file failed")
          Behaviors.same
      }
    }

}
