package enderman.models.repository

import enderman.models.Business
import org.mongodb.scala.MongoCollection

import scala.concurrent.{ ExecutionContext, Future }

class BusinessRepository(
  val collection: MongoCollection[Business])(
  implicit
  val ec: ExecutionContext) extends ClientInfo[Business] {

  def insertOne(business: Business): Future[String] =
    collection
      .insertOne(business)
      .head
      .map { _ => business._id.toHexString }

}
