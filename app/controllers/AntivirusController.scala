package controllers

import cats.implicits.catsSyntaxOption
import controllers.AntivirusController.MaxFileNameLength
import controllers.Token.HashedToken
import controllers.Token.validateToken
import controllers.error.ApiError.FileNameTooLong
import controllers.error.ApiError.MalformedFileKey
import controllers.error.AppErrorTransformer.handleError
import play.api.Logger
import play.api.libs.Files
import play.api.mvc.AbstractController
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import play.api.mvc.MultipartFormData
import service.AntivirusService

import java.io.File
import java.nio.file.Paths
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AntivirusController(
    controllerComponents: ControllerComponents,
    token: HashedToken,
    downloadDirectory: String,
    antivirusService: AntivirusService
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  val logger: Logger = Logger(this.getClass)

  def test() = Action.async { request =>
    val app =
      for {
        _   <- validateToken(request, token)
        res <- Future.successful(NoContent)
      } yield res

    app.recover { case err => handleError(request, err) }

  }

  def uploadReportFile: Action[MultipartFormData[Files.TemporaryFile]] =
    Action.async(parse.multipartFormData) { request =>
      val app = for {
        filePart <- request.body.file("reportFile").liftTo[Future](MalformedFileKey("reportFile"))
        _ <-
          if (filePart.filename.length > MaxFileNameLength) {
            Future.failed(FileNameTooLong(filePart.filename))
          } else Future.unit
        externalId = request.body.dataParts
          .get("id")
          .flatMap(o => o.headOption)
        fileExtension = filePart.filename.toLowerCase.split("\\.").last
        tmpFile       = pathFromFilePart(filePart)

      } yield NoContent

      app.recover { case err => handleError(request, err) }
    }

  private def pathFromFilePart(filePart: MultipartFormData.FilePart[Files.TemporaryFile]): File = {
    val filename = Paths.get(filePart.filename).getFileName
    val tmpFile =
      new java.io.File(s"${downloadDirectory}/${UUID.randomUUID}_${filename}")
    filePart.ref.copyTo(tmpFile): Unit
    tmpFile
  }

}

object AntivirusController {
  val FileKey           = "reportFile"
  val MaxFileNameLength = 200
}
