package models

import play.api.libs.json._

import java.time.OffsetDateTime

case class ScanCommand(
    externalId: String,
    filename: String
)
object ScanCommand {
  implicit val fileFormat: OFormat[ScanCommand] = Json.format[ScanCommand]
}
