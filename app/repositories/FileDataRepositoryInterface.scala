package repositories

import models.FileData
import models.FileId

import scala.concurrent.Future

trait FileDataRepositoryInterface {

  def create(fileData: FileData): Future[FileData]

  def setAvOutput(fileId: FileId, output: String): Future[Int]

  def removeStorageFileName(fileId: FileId): Future[Int]

}
