package enderman.models.repository

import enderman.models.Location
import org.mongodb.scala.MongoCollection

import scala.concurrent.{ ExecutionContext, Future }

class LocationRepository(collection: MongoCollection[Location])(implicit ec: ExecutionContext) {

  def insertOne(location: Location): Future[String] =
    collection
      .insertOne(location)
      .head
      .map { _ => location._id.toHexString }

}
