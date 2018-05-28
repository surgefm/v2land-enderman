package enderman.models.repository

import enderman.models.Business
import org.mongodb.scala.MongoCollection

import scala.concurrent.{ ExecutionContext, Future }

class BusinessRepository(collection: MongoCollection[Business])(implicit ec: ExecutionContext) {

  def insertOne(business: Business): Future[String] =
    collection
      .insertOne(business)
      .head
      .map { _ => business._id.toHexString }

}
