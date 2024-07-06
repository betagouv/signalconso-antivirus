package models

import play.api.libs.json._

import java.time.OffsetDateTime

case class FileData(
    id: FileId,
    creationDate: OffsetDateTime,
    filename: String,
    storageFilename: String,
    avOutput: Option[String]
)
object FileData {
  implicit val fileFormat: OFormat[FileData] = Json.format[FileData]
}
