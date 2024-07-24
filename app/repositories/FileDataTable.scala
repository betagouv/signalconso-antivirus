package repositories

import models.FileData
import models.FileId
import repositories.PostgresProfile.api._
import repositories.FileDataTable.FileIdColumnType
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.Rep

import java.time.OffsetDateTime
import java.util.UUID
class FileDataTable(tag: Tag)
    extends Table[FileData](_tableTag = tag, _schemaName = Some("signalconso_antivirus"), _tableName = "file_data") {

  val id: Rep[FileId] = column[FileId]("id", O.PrimaryKey)
  def externalId      = column[String]("external_id")
  def creationDate    = column[OffsetDateTime]("creation_date")
  def filename        = column[String]("filename")
  def scanResult      = column[Option[Int]]("scan_result")
  def avOutput        = column[Option[String]]("av_output")

  type FileDataTuple = (FileId, String, OffsetDateTime, String, Option[Int], Option[String])

  def constructFile: FileDataTuple => FileData = {
    case (id, externalId, creationDate, filename, scanResult, avOutput) =>
      FileData(id, externalId, creationDate, filename, scanResult, avOutput)
  }

  def extractFile: PartialFunction[FileData, FileDataTuple] = {
    case FileData(id, externalId, creationDate, filename, scanResult, avOutput) =>
      (id, externalId, creationDate, filename, scanResult, avOutput)
  }

  def * =
    (
      id,
      externalId,
      creationDate,
      filename,
      scanResult,
      avOutput
    ) <> (constructFile, extractFile.lift)
}

object FileDataTable {
  val table = TableQuery[FileDataTable]

  implicit val FileIdColumnType: JdbcType[FileId] with BaseTypedType[FileId] =
    MappedColumnType.base[FileId, UUID](
      _.value,
      FileId(_)
    )
}
