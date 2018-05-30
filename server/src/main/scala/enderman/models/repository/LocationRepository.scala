package enderman.models.repository

import enderman.models.Location
import org.mongodb.scala.MongoCollection

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

}
