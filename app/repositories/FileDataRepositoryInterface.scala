package repositories

import models.FileId

import scala.concurrent.Future

trait FileDataRepositoryInterface {

  def setAvOutput(fileId: FileId, output: String): Future[Int]

  def removeStorageFileName(fileId: FileId): Future[Int]

}
