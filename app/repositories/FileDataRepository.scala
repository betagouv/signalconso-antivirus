package repositories

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

  override def setAvOutput(fileId: FileId, output: String): Future[Int] = db
    .run(
      table
        .filter(_.id === fileId)
        .map(_.avOutput)
        .update(Some(output))
    )

  override def removeStorageFileName(fileId: FileId): Future[Int] = db
    .run(
      table
        .filter(_.id === fileId)
        .map(_.storageFilename)
        .update("")
    )

}
