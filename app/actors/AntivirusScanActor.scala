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
  final case class ScanFromFile(fileData: FileData, file: java.io.File)             extends ScanCommand
  final case class ScanFromFileSuccess(fileData: FileData)                          extends ScanCommand
  final case class ScanFromFileFailed(throwable: Throwable)                         extends ScanCommand
  final case class ScanFromBucket(reportFile: FileData)                             extends ScanCommand
  final case class ScanFromBucketFailed(reportFile: FileData, throwable: Throwable) extends ScanCommand

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
  ): Behavior[ScanCommand] = {

    def getFile(file: java.io.File, filePath: String, filename: String)(implicit ec: ExecutionContext) =
      if (file.exists()) {
        Future.successful(file)
      } else {
        logger.infoWithTitle("get_file", s"get file $filename on path $filePath")
        s3Service
          .downloadOnCurrentHost(filename, filePath)
          .map(_ => new File(filePath))
      }

    Behaviors.setup { context =>
      implicit val ec: ExecutionContext =
        context.system.dispatchers.lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"))

      Behaviors.receiveMessage[ScanCommand] {
        case ScanFromBucket(fileData: FileData) =>
          logger.infoWithTitle(
            "scan_file",
            s"scanning file ${fileData.id.value} : ${fileData.filename}"
          )
          val filePath = s"${uploadConfiguration.tmpDirectory}/${fileData.filename}"
          val file     = new File(filePath)
          context.pipeToSelf(getFile(file, filePath, fileData.filename)) {
            case Success(_) =>
              ScanFromFile(fileData, file)
            case Failure(e) =>
              ScanFromBucketFailed(fileData, e)
          }
          Behaviors.same

        case ScanFromBucketFailed(reportFile: FileData, throwable: Throwable) =>
          logger.errorWithTitle(
            "scan_file_bucket_failure",
            s"Failed to get file from bucket ${reportFile.filename}",
            throwable
          )
          Behaviors.same

        case ScanFromFile(fileData: FileData, file: java.io.File) =>
          val filePath = s"${uploadConfiguration.tmpDirectory}/${fileData.filename}"
          val result = for {
            existingFile <- getFile(file, fileData.filename, filePath)
            _ = logger.debug("Begin Antivirus scan.")
            antivirusScanResult <- performAntivirusScan(existingFile)
            _ = logger.debug(
              s"Saving output to database : ${antivirusScanResult.output}"
            )
            _ = antivirusScanResult.exitCode.map(_.value)
            _ <- reportFileRepository.setScanResult(
              fileData.id,
              antivirusScanResult.exitCode.map(_.value),
              antivirusScanResult.output
            )
            _ <- antivirusScanResult.exitCode match {
              case Some(NoVirusFound) | None =>
                logger.infoWithTitle(
                  "scan_no_virus",
                  s"scanning file success ${fileData.id.value} : ${fileData.filename}"
                )
                logger.debug("Deleting file.")
                Future.successful(existingFile.delete())
              case Some(VirusFound) =>
                logger.warnWithTitle(
                  "scan_found_virus",
                  s"Antivirus scan found virus, scan output : ${antivirusScanResult.output}"
                )
                logger.debug(s"File has been deleted by Antivirus, removing file from S3")
                s3Service
                  .delete(fileData.filename)
              case Some(ErrorOccured) =>
                logger.errorWithTitle(
                  "scan_unexpected_error",
                  s"Unexpected error occured when running scan on file $filePath : ${antivirusScanResult.output}"
                )
                Future.successful(Done)
            }
          } yield Done

          context.pipeToSelf(result) {
            case Success(_) => ScanFromFileSuccess(fileData)
            case Failure(e) => ScanFromFileFailed(e)
          }

          Behaviors.same

        case ScanFromFileSuccess(fileData) =>
          logger.infoWithTitle(
            "scan_file_done",
            s"scanning file success ${fileData.id.value} : ${fileData.filename}"
          )
          Behaviors.same
        case ScanFromFileFailed(e) =>
          logger.warnWithTitle("scan_attempt_failed", s"Antivirus has not been called properly", e)
          Behaviors.same
      }
    }
  }

}
