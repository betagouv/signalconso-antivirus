package models

import play.api.libs.json._

case class ScanCommand(
    externalId: String,
    filename: String
)
object ScanCommand {
  implicit val fileFormat: OFormat[ScanCommand] = Json.format[ScanCommand]
}
