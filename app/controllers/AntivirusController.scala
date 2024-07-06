package controllers

import controllers.Token.HashedToken
import controllers.Token.validateToken
import controllers.error.AppErrorTransformer.handleError
import play.api.Logger
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class AntivirusController(
    controllerComponents: ControllerComponents,
    token: HashedToken
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

}
