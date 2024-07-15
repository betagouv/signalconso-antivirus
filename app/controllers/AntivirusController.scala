package controllers

import cats.implicits.catsSyntaxOption
import controllers.AntivirusController.FileIdKey
import controllers.AntivirusController.FileKey
import controllers.AntivirusController.MaxFileNameLength
import controllers.Token.HashedToken
import controllers.error.ApiError.FileNameTooLong
import controllers.error.ApiError.MalformedFileKey
import controllers.error.ApiError.MissingExternalId
import controllers.error.AppErrorTransformer.handleError
import models.ScanCommand
import play.api.Logger
import play.api.libs.Files
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.ControllerComponents
import play.api.mvc.MultipartFormData
import service.AntivirusService

import java.io.File
import java.nio.file.Paths
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AntivirusController(
    controllerComponents: ControllerComponents,
    token: HashedToken,
    downloadDirectory: String,
    antivirusService: AntivirusService
)(implicit val ec: ExecutionContext)
    extends BaseController(controllerComponents, token) {

  val logger: Logger = Logger(this.getClass)

  def scanAndUpload: Action[MultipartFormData[Files.TemporaryFile]] =
    SecuredAction.async(parse.multipartFormData) { request =>
      val app = for {
        filePart <- request.body.file(FileKey).liftTo[Future](MalformedFileKey(FileKey))
        _ <-
          if (filePart.filename.length > MaxFileNameLength) {
            Future.failed(FileNameTooLong(filePart.filename))
          } else Future.unit
        externalId <- request.body.dataParts
          .get(FileIdKey)
          .flatMap(o => o.headOption)
          .liftTo[Future](MissingExternalId)
        fileName = filePart.filename
        file     = pathFromFilePart(filePart)
        _ <- antivirusService.scanFromFile(externalId, fileName, file)
      } yield NoContent

      app.recover { case err => handleError(request, err) }
    }

  def scan() =
    SecuredAction.async(parse.json) { request =>
      request.body
        .validate[ScanCommand]
        .fold(
          errors => Future.successful(BadRequest(JsError.toJson(errors))),
          results => antivirusService.scan(results).map(_ => NoContent)
        )
    }

  def rescan() =
    SecuredAction.async(parse.json) { request =>
      request.body
        .validate[List[ScanCommand]]
        .fold(
          errors => Future.successful(BadRequest(JsError.toJson(errors))),
          results => antivirusService.reScanFile(results).map(_ => NoContent)
        )
    }

  def fileStatus(externalFileId: String) = SecuredAction.async(parse.empty) { request =>
    antivirusService
      .fileStatus(externalFileId)
      .map(fileData => Ok(Json.toJson(fileData)))
      .recover { case err => handleError(request, err) }
  }

  private def pathFromFilePart(filePart: MultipartFormData.FilePart[Files.TemporaryFile]): File = {
    val filename = Paths.get(filePart.filename).getFileName
    val tmpFile =
      new java.io.File(s"${downloadDirectory}/${filename}")
    filePart.ref.copyTo(tmpFile): Unit
    tmpFile
  }

}

object AntivirusController {
  val FileKey           = "file"
  val FileIdKey         = "id"
  val MaxFileNameLength = 200
}
