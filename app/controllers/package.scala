import cats.syntax.either._

import controllers.error.ApiError.MalformedBody
import play.api.Logger
import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.mvc.QueryStringBindable
import play.api.mvc.Request

import java.util.Locale
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

package object controllers {

  val logger: Logger = Logger(this.getClass)
  implicit class RequestOps[T <: JsValue](request: Request[T])(implicit ec: ExecutionContext) {
    def parseBody[B](path: JsPath = JsPath())(implicit reads: Reads[B]) = request.body
      .validate[B](path.read[B])
      .asEither
      .leftMap { errors =>
        logger.error(
          s"Malformed request body path ${path} [error : ${JsError.toJson(errors)} , body ${request.body} ]"
        )
        MalformedBody
      }
      .liftTo[Future]
  }

  implicit val LocaleQueryStringBindable: QueryStringBindable[Locale] =
    QueryStringBindable.bindableString
      .transform[Locale](
        locale => Locale.forLanguageTag(locale),
        locale => locale.toLanguageTag
      )

}
