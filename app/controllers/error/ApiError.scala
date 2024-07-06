package controllers.error

sealed trait AppError extends Throwable with Product with Serializable

case class InseeTokenGenerationError(message: String)          extends AppError
case class InseeEtablissementError(message: String)            extends Exception(message) with AppError
case class EtablissementJobAleadyRunningError(message: String) extends Exception(message) with AppError
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

  final case class MalformedSiret(InvalidSiret: String) extends BadRequestError {
    override val `type`: String = "SC-0003"
    override val title: String  = "Malformed SIRET"
    override val details: String =
      s"Malformed SIRET : $InvalidSiret"
  }

  final case class MalformedSiren(InvalidSIREN: String) extends BadRequestError {
    override val `type`: String = "SC-0007"
    override val title: String  = "Malformed SIREN"
    override val details: String =
      s"Malformed SIREN : $InvalidSIREN"
  }

  final case class MalformedId(id: String) extends BadRequestError {
    override val `type`: String = "SC-0004"
    override val title: String  = "Malformed id"
    override val details: String =
      s"Malformed id : $id"
  }

  final case class MalformedValue(value: String, expectedValidType: String) extends BadRequestError {
    override val `type`: String = "SC-0005"
    override val title: String  = s"Malformed value, $value is not a valid value, expecting valid $expectedValidType"
    override val details: String =
      s"La valeur $value ne correspond pas à ce qui est attendu par l'API. Merci de renseigner une valeur valide pour $expectedValidType"
  }

  final case object InvalidToken extends UnauthorizedError {
    override val `type`: String = "SC-0006"
    override val title: String  = s"Token is malformed"
    override val details: String =
      s"Token is malformed or not found"
  }
}
