package service

import actors.AntivirusScanActor
import models.FileData
import models.FileId
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.FileIO
import play.api.Logger
import repositories.FileDataRepositoryInterface

import java.time.OffsetDateTime
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AntivirusService(
    antivirusScanActor: ActorRef[AntivirusScanActor.ScanCommand],
    s3Service: S3ServiceInterface,
    fileDataRepository: FileDataRepositoryInterface
)(implicit val executionContext: ExecutionContext, mat: Materializer) {
  val logger = Logger(this.getClass)

  def saveReportFile(externalId: String, filename: String, file: java.io.File): Future[Unit] =
    for {
      reportFile <- fileDataRepository.create(
        FileData(
          FileId.generateId(),
          externalId = externalId,
          creationDate = OffsetDateTime.now(),
          filename = filename,
          storageFilename = file.getName(),
          avOutput = None
        )
      )
      _ <- FileIO
        .fromPath(file.toPath)
        .to(s3Service.upload(reportFile.storageFilename))
        .run()
      _ = logger.debug(s"Uploaded file ${reportFile.id} to S3")
    } yield {
      antivirusScanActor ! AntivirusScanActor.ScanFromFile(reportFile, file)
      reportFile
    }

}
