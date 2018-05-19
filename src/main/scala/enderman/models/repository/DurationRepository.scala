package enderman.models.repository

import enderman.models.Duration
import org.mongodb.scala.MongoCollection

import scala.concurrent.{ ExecutionContext, Future }

class DurationRepository(collection: MongoCollection[Duration])(implicit ec: ExecutionContext) {

  def insertOne(duration: Duration): Future[String] =
    collection
      .insertOne(duration)
      .head
      .map { _ => duration._id.toHexString }

}
