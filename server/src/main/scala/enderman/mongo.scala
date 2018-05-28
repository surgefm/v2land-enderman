package enderman

import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import models._

object mongo extends Config {

  lazy val mongoClient = MongoClient(config.getString("mongo.uri"))
  lazy val codecRegistry = fromRegistries(
    fromProviders(classOf[ClientInfo]),
    fromProviders(classOf[Duration]),
    fromProviders(classOf[Location]),
    fromProviders(classOf[Business]),
    fromProviders(classOf[ContextScript]),
    DEFAULT_CODEC_REGISTRY)
  lazy val database = mongoClient.getDatabase(config.getString("mongo.database")).withCodecRegistry(codecRegistry)

  lazy val durationCollection: MongoCollection[Duration] = database.getCollection[Duration]("duration")
  lazy val locationCollection: MongoCollection[Location] = database.getCollection[Location]("location")
  lazy val businessCollection: MongoCollection[Business] = database.getCollection[Business]("business")
  lazy val contextScriptCollection: MongoCollection[ContextScript] = database.getCollection[ContextScript]("contextScript")

}
