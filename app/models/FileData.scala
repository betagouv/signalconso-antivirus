package models

import play.api.libs.json._

import java.time.OffsetDateTime

case class FileData(
    id: FileId,
    externalId: String,
    creationDate: OffsetDateTime,
    filename: String,
    scanResult: Option[Int],
    avOutput: Option[String]
)
object FileData {
  implicit val fileFormat: OFormat[FileData] = Json.format[FileData]
}
