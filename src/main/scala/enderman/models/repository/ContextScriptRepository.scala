package enderman.models.repository

import enderman.models.ContextScript
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document

import scala.concurrent.{ ExecutionContext, Future }

class ContextScriptRepository(collection: MongoCollection[ContextScript])(implicit ec: ExecutionContext) {

  def latestContent(): Future[String] =
    collection
      .find()
      .sort(Document("date" -> -1))
      .head
      .map(_.content)

  def insertOne(contextScript: ContextScript): Future[String] =
    collection
      .insertOne(contextScript)
      .head
      .map { _ => contextScript._id.toHexString }

}
