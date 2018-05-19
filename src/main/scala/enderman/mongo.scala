package enderman

import com.typesafe.config.ConfigFactory
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import models.{ Duration, Location, Business }

object mongo {

  lazy val config = ConfigFactory.load()
  lazy val mongoClient = MongoClient(config.getString("mongo.uri"))
  lazy val codecRegistry = fromRegistries(
    fromProviders(classOf[Duration]),
    fromProviders(classOf[Location]),
    fromProviders(classOf[Business]),
    DEFAULT_CODEC_REGISTRY)
  lazy val database = mongoClient.getDatabase(config.getString("mongo.database")).withCodecRegistry(codecRegistry)

  lazy val durationCollection: MongoCollection[Duration] = database.getCollection[Duration]("duration")
  lazy val locationCollection: MongoCollection[Location] = database.getCollection[Location]("location")
  lazy val businessCollection: MongoCollection[Business] = database.getCollection[Business]("business")

}
