package service

import actors.AntivirusScanActor
import actors.antivirus.AntivirusScanExitCode
import cats.implicits.catsSyntaxOption
import controllers.error.ApiError.UnknownExternalId
import models.FileData
import models.FileId
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.stream.Materializer
import play.api.Logger
import repositories.FileDataRepositoryInterface

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AntivirusService(
    antivirusScanActor: ActorRef[AntivirusScanActor.ScanCommand],
    fileDataRepository: FileDataRepositoryInterface
)(implicit val executionContext: ExecutionContext, mat: Materializer) {
  val logger = Logger(this.getClass)

  def scanAndSave(externalId: String, filename: String, file: java.io.File): Future[Unit] =
    for {
      reportFile <- fileDataRepository.create(
        FileData(
          FileId.generateId(),
          externalId = externalId,
          creationDate = OffsetDateTime.now(),
          filename = filename,
          scanResult = None,
          avOutput = None
        )
      )
      _ = logger.debug(s"Uploaded file ${reportFile.id} to S3")
    } yield {
      antivirusScanActor ! AntivirusScanActor.ScanFromFile(reportFile, file)
      reportFile
    }

  def reScanFile(fileExternalIds: List[String]) = fileDataRepository
    .getByExternalId(fileExternalIds)
    .map { files =>
      println(s"------------------ files = ${files} ------------------")
      files
        .filter(f => f.scanResult.isEmpty || f.scanResult.contains(AntivirusScanExitCode.ErrorOccured.value))
        .map(file => antivirusScanActor ! AntivirusScanActor.ScanFromBucket(file))
    }

  def fileStatus(externalFileId: String): Future[FileData] =
    for {
      fileDataOpt <- fileDataRepository.getByExternalId(List(externalFileId)).map(_.headOption)
      fileData    <- fileDataOpt.liftTo[Future](UnknownExternalId(externalFileId))
    } yield fileData

}
