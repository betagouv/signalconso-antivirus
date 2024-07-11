package repositories

import models.FileData
import models.FileId
import repositories.PostgresProfile.api._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import FileDataTable.FileIdColumnType

class FileDataRepository(val dbConfig: DatabaseConfig[JdbcProfile])(implicit
    val ec: ExecutionContext
) extends FileDataRepositoryInterface {

  val table: TableQuery[FileDataTable] = FileDataTable.table

  import dbConfig._

  def create(fileData: FileData): Future[FileData] =
    db.run(table += fileData).map(_ => fileData)

  override def setAvOutput(fileId: FileId, output: String): Future[Int] = db
    .run(
      table
        .filter(_.id === fileId)
        .map(_.avOutput)
        .update(Some(output))
    )

  override def getByExternalId(ids: List[String]): Future[List[FileData]] = db.run {
    table
      .filter(_.externalId inSet ids)
      .to[List]
      .result
  }

  override def setScanResult(fileId: FileId, result: Option[Int], output: String): Future[Int] = db
    .run(
      table
        .filter(_.id === fileId)
        .map(file => (file.avOutput, file.scanResult))
        .update((Some(output), result))
    )
}
