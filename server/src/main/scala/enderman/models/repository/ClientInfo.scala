package enderman.models.repository

import java.util.Date

import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

trait ClientInfo[TResult] {

  val collection: MongoCollection[TResult]
  implicit val ec: ExecutionContext

  def findBetweenDate(
    beginDate: Date,
    endDate: Date = new Date)(
    implicit
    ct: ClassTag[TResult]): Future[Seq[TResult]] =
    collection
      .find(
        Document(
          "clientInfo.date" -> Document(
            "$lt" -> endDate,
            "$gte" -> beginDate)))
      .toFuture()

}
