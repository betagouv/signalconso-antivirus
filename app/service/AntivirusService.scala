package service

import actors.AntivirusScanActor
import actors.antivirus.AntivirusScanExitCode
import cats.implicits.catsSyntaxOption
import controllers.error.ApiError.UnknownExternalId
import models.{FileData, FileId, ScanCommand}
import org.apache.pekko.actor.typed.ActorRef
import play.api.Logger
import repositories.FileDataRepositoryInterface

import java.time.OffsetDateTime
import scala.concurrent.{ExecutionContext, Future}

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

  def reScanFile(fileExternalIds: List[String]) = fileDataRepository
    .getByExternalId(fileExternalIds)
    .map { files =>
      files
        .filter(f => f.scanResult.isEmpty || f.scanResult.contains(AntivirusScanExitCode.ErrorOccured.value))
        .map(file => antivirusScanActor ! AntivirusScanActor.ScanFromBucket(file))
    }

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
    } yield fileData

}
