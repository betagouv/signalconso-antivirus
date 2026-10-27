package controllers

import buildinfo.BuildInfo
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.Action
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class HealthController(
    controllerComponents: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def health: Action[Unit] =
    Action.async(parse.empty) { _ =>
      Future.successful(
        Ok(
          Json.obj(
            "name"      -> "signalconso-antivirus",
            "version"   -> BuildInfo.version,
            "gitCommit" -> BuildInfo.gitCommit,
            "buildTime" -> BuildInfo.buildTime
          )
        )
      )
    }

}
