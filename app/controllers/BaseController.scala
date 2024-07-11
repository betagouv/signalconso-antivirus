package controllers

import controllers.Token.HashedToken
import controllers.error.AppErrorTransformer
import controllers.error.AppErrorTransformer.handleError
import play.api.mvc._

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class ErrorHandlerActionFunction[R[_] <: play.api.mvc.Request[_]](
    getIdentity: R[_] => Option[UUID] = (_: R[_]) => None
)(implicit
    ec: ExecutionContext
) extends ActionFunction[R, R] {

  def invokeBlock[A](
      request: R[A],
      block: R[A] => Future[Result]
  ): Future[Result] =
    // An error may happen either in the result of the future,
    // or when building the future itself
    Try {
      block(request).recover { case err =>
        handleError(request, err, getIdentity(request))
      }
    } match {
      case Success(res) => res
      case Failure(err) =>
        Future.successful(handleError(request, err, getIdentity(request)))
    }

  override protected def executionContext: ExecutionContext = ec
}

abstract class BaseController(
    override val controllerComponents: ControllerComponents,
    token: HashedToken
) extends AbstractController(controllerComponents) {

  implicit val ec: ExecutionContext

  def SecuredAction = new SecuredApiAction(
    new BodyParsers.Default(controllerComponents.parsers),
    token
  ) andThen new ErrorHandlerActionFunction[Request]()
}

class SecuredApiAction(val parser: BodyParsers.Default, token: HashedToken)(implicit
    val executionContext: ExecutionContext
) extends ActionBuilder[Request, AnyContent]
    with ActionRefiner[Request, Request] {
  override protected def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] =
    Token.validateToken(request, token) match {
      case Right(_) => Future.successful(Right(request))
      case Left(error) =>
        val result = AppErrorTransformer.handleError(request, error)
        Future.successful(Left(result))
    }
}
