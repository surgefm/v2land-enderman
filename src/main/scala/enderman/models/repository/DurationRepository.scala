package enderman.models.repository

import java.util.Date

import enderman.models.Duration
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{ ExecutionContext, Future }

class DurationRepository(collection: MongoCollection[Duration])(implicit ec: ExecutionContext) {

  def insertOne(duration: Duration): Future[String] =
    collection
      .insertOne(duration)
      .head
      .map { _ => duration._id.toHexString }

  def findBetweenDate(beginDate: Date, endDate: Date = new Date): Future[Seq[Duration]] =
    collection
      .find(Document(
        "clientInfo.date" -> Document(
          "$lt" -> endDate,
          "$gte" -> beginDate)))
      .toFuture()

}
