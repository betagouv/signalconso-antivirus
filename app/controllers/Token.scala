package controllers

import cats.implicits.catsSyntaxOption
import controllers.error.ApiError.InvalidToken
import de.mkammerer.argon2.Argon2Factory
import org.apache.pekko.util.ByteString
import play.api.mvc.Request

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object Token {
  private val argon = Argon2Factory.create

  private val ArgonIterations  = 10
  private val ArgonMemory      = 65536
  private val ArgonParallelism = 1

  case class HashedToken(value: String) extends AnyVal
  case class ClearToken(value: String)  extends AnyVal

  // To be used to generate token
  def hash(clearToken: ClearToken): HashedToken =
    HashedToken(
      argon.hash(ArgonIterations, ArgonMemory, ArgonParallelism, bytes(clearToken))
    )

  def matches(clear: ClearToken, hashed: HashedToken): Boolean =
    argon.verify(hashed.value, bytes(clear))

  private def bytes(token: ClearToken): Array[Byte] =
    token.value.getBytes(ByteString.UTF_8)

  def validateToken(request: Request[_], token: HashedToken)(implicit e: ExecutionContext): Future[Unit] =
    for {
      clearToken <- request.headers.get("X-Api-Key").map(ClearToken).liftTo[Future](InvalidToken)
      _          <- if (Token.matches(clearToken, token)) Future.unit else Future.failed(InvalidToken)
    } yield ()

}
