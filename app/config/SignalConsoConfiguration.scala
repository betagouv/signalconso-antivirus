package config

import cats.implicits.catsSyntaxEitherId
import controllers.Token.HashedToken
import pureconfig.ConfigReader
import pureconfig.error.FailureReason

case class SignalConsoConfiguration(
    tmpDirectory: String,
    apiAuthenticationToken: HashedToken
)

object SignalConsoConfiguration {

  implicit val HashedTokenReader: ConfigReader[HashedToken] =
    ConfigReader
      .fromString(s => HashedToken(s).asRight: Either[FailureReason, HashedToken])

}
