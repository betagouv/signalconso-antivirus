package models

import play.api.libs.json.Format
import play.api.libs.json.Json

import java.util.UUID

case class FileId(value: UUID) extends AnyVal

object FileId {
  implicit val ReportFileIdFormat: Format[FileId] =
    Json.valueFormat[FileId]

  def generateId() = new FileId(UUID.randomUUID())
}
