package controllers.error

import controllers.AntivirusController.FileKey
import controllers.AntivirusController.MaxFileNameLength

sealed trait AppError extends Throwable with Product with Serializable

sealed trait ApiError extends AppError {
  val `type`: String
  val title: String
  val details: String
}

sealed trait UnauthorizedError extends ApiError
sealed trait NotFoundError     extends ApiError
sealed trait BadRequestError   extends ApiError
sealed trait ForbiddenError    extends ApiError
sealed trait ConflictError     extends ApiError
sealed trait InternalAppError  extends ApiError
sealed trait PreconditionError extends ApiError

object ApiError {

  final case class ServerError(message: String, cause: Option[Throwable] = None) extends InternalAppError {
    override val `type`: String  = "SC-0001"
    override val title: String   = message
    override val details: String = "Une erreur inattendue s'est produite."
  }

  final case object MalformedBody extends BadRequestError {
    override val `type`: String  = "SC-0002"
    override val title: String   = "Malformed request body"
    override val details: String = s"Le corps de la requête ne correspond pas à ce qui est attendu par l'API."
  }
  final case class MalformedFileKey(value: String) extends BadRequestError {
    override val `type`: String  = "SC-0003"
    override val title: String   = s"Malformed filekey, expecting $FileKey"
    override val details: String = s"Malformed filekey, expecting $FileKey"
  }

  final case class FileNameTooLong(name: String) extends BadRequestError {
    override val `type`: String = "SC-0004"
    override val title: String  = "FileNameTooLong"
    override val details: String =
      s"FileName too long :$name , expecting less than $MaxFileNameLength"
  }

  final case object InvalidToken extends UnauthorizedError {
    override val `type`: String = "SC-0006"
    override val title: String  = s"Token is malformed"
    override val details: String =
      s"Token is malformed or not found"
  }
}
