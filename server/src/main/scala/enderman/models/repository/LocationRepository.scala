package enderman.models.repository

import enderman.models.Location
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{ ExecutionContext, Future }

class LocationRepository(
  val collection: MongoCollection[Location])(
  implicit
  val ec: ExecutionContext) extends ClientInfo[Location] {

  def insertOne(location: Location): Future[String] =
    collection
      .insertOne(location)
      .head
      .map { _ => location._id.toHexString }

  def findMostViewedLocations: Future[Seq[(String, Int)]] = {
    collection.aggregate[Document](List(
      Document(
        "$group" -> Document(
          "_id" -> "$url",
          "count" -> Document("$sum" -> 1))),
      Document(
        "$sort" -> Document("count" -> -1))))
      .toFuture()
      .map { result =>
        result.slice(0, 3).map { item =>
          (
            item("_id").asString().getValue,
            item("count").asInt32().getValue)
        }
      }
  }

}
