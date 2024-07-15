package service

import actors.AntivirusScanActor
import actors.antivirus.AntivirusScanExitCode
import cats.implicits.catsSyntaxOption
import cats.implicits.toTraverseOps
import controllers.error.ApiError.UnknownExternalId
import models.FileData
import models.FileId
import models.ScanCommand
import org.apache.pekko.actor.typed.ActorRef
import play.api.Logger
import repositories.FileDataRepositoryInterface

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AntivirusService(
    antivirusScanActor: ActorRef[AntivirusScanActor.ScanCommand],
    fileDataRepository: FileDataRepositoryInterface
)(implicit val executionContext: ExecutionContext) {
  val logger = Logger(this.getClass)

  def scanFromFile(externalId: String, filename: String, file: java.io.File): Future[FileData] =
    for {
      fileData <- createFileData(
        externalId = externalId,
        filename = filename
      )
      _ = logger.debug(s"Scheduling scan for ${fileData.filename}")
    } yield {
      antivirusScanActor ! AntivirusScanActor.ScanFromFile(fileData, file)
      fileData
    }

  def scan(scanCommand: ScanCommand): Future[FileData] =
    for {
      fileData <-
        createFileData(
          externalId = scanCommand.externalId,
          filename = scanCommand.filename
        )
      _ = logger.debug(s"Scheduling scan for ${fileData.filename}")
    } yield {
      antivirusScanActor ! AntivirusScanActor.ScanFromBucket(fileData)
      fileData
    }

  def reScanFile(fileExternalIds: List[ScanCommand]) =
    for {
      files <- fileDataRepository.getByExternalId(fileExternalIds.map(_.externalId))
      // Creating missing files
      _ <- fileExternalIds
        .filterNot(f => files.map(_.externalId).contains(f.externalId))
        .traverse(c =>
          createFileData(
            externalId = c.externalId,
            filename = c.filename
          )
        )
      _ = files
        .filter(f => f.scanResult.isEmpty || f.scanResult.contains(AntivirusScanExitCode.ErrorOccured.value))
        .map(file => antivirusScanActor ! AntivirusScanActor.ScanFromBucket(file))
    } yield ()

  private def createFileData(externalId: String, filename: String): Future[FileData] = fileDataRepository.create(
    FileData(
      FileId.generateId(),
      externalId = externalId,
      creationDate = OffsetDateTime.now(),
      filename = filename,
      scanResult = None,
      avOutput = None
    )
  )

  def fileStatus(externalFileId: String): Future[FileData] =
    for {
      fileDataOpt <- fileDataRepository.getByExternalId(List(externalFileId)).map(_.headOption)
      fileData    <- fileDataOpt.liftTo[Future](UnknownExternalId(externalFileId))
      status = fileData.scanResult.filter(i =>
        i == AntivirusScanExitCode.VirusFound.value || i == AntivirusScanExitCode.NoVirusFound.value
      )
    } yield fileData.copy(scanResult = status)

}
