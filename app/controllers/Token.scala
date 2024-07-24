package controllers

import controllers.error.ApiError.InvalidToken
import de.mkammerer.argon2.Argon2Factory
import org.apache.pekko.util.ByteString
import play.api.mvc.Request

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

  def validateToken(request: Request[_], token: HashedToken) =
    for {
      clearToken <- request.headers.get("X-Api-Key").map(ClearToken).toRight(InvalidToken)
      res        <- Either.cond(Token.matches(clearToken, token), request, InvalidToken)
    } yield res

}
