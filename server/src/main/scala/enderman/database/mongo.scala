package enderman.database

import enderman.{ Config, JsonSupport }
import enderman.models._
import org.bson.{ BsonReader, BsonWriter, json }
import org.bson.codecs.{ Codec, DecoderContext, EncoderContext }
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object mongo {

  object BusinessCodec extends Codec[Business] with JsonSupport {
    import spray.json._

    override def encode(writer: BsonWriter, value: Business, encoderContext: EncoderContext): Unit = {

      writer.writeStartDocument()
      writer.writeName("_id")
      writer.writeObjectId(value._id)
      writer.writeName("action")
      writer.writeString(value.action)
      writer.writeName("meta")
      writer.pipe(new json.JsonReader(value.meta))
      writer.writeName("clientInfo")

      writer.writeStartDocument()
      writer.writeName("clientIp")
      writer.writeString(value.clientInfo.clientIp)
      writer.writeName("userAgent")
      writer.writeString(value.clientInfo.userAgent)
      writer.writeName("sessionId")
      writer.writeString(value.clientInfo.sessionId)
      writer.writeName("userId");
      value.clientInfo.userId match {
        case Some(idVal) => writer.writeString(idVal)
        case None => writer.writeNull()
      }
      writer.writeName("date")
      writer.writeDateTime(value.clientInfo.date.getTime)
      writer.writeEndDocument()

      writer.writeEndDocument()
    }

    override def getEncoderClass: Class[Business] = classOf[Business]

    override def decode(reader: BsonReader, decoderContext: DecoderContext): Business = {
      reader.readStartDocument()
      reader.readName("_id")
      val _id = reader.readObjectId()
      reader.readName("action")
      val action = reader.readString()
      reader.readName("meta")
      val meta = reader.readJavaScript("meta")
      reader.readName("clientInfo")
      val clientInfo = reader.readJavaScript("clientInfo")
      reader.readEndDocument()

      Business(
        _id,
        action,
        meta,
        clientInfo.parseJson.convertTo[ClientInfo])
      //      val jsonStr = reader.readJavaScript()
      //      val jsonData = jsonStr.parseJson
      //      jsonData.convertTo[Business]
    }
  }

  lazy val mongoClient = MongoClient(Config.config.getString("mongo.uri"))
  lazy val codecRegistry = fromRegistries(
    fromCodecs(BusinessCodec),
    fromProviders(classOf[ClientInfo]),
    fromProviders(classOf[Duration]),
    fromProviders(classOf[Location]),
    //    fromProviders(classOf[Business]),
    fromProviders(classOf[ContextScript]),
    DEFAULT_CODEC_REGISTRY)
  lazy val database = mongoClient.getDatabase(Config.config.getString("mongo.database")).withCodecRegistry(codecRegistry)

  lazy val durationCollection: MongoCollection[Duration] = database.getCollection[Duration]("duration")
  lazy val locationCollection: MongoCollection[Location] = database.getCollection[Location]("location")
  lazy val businessCollection: MongoCollection[Business] = database.getCollection[Business]("business")
  lazy val contextScriptCollection: MongoCollection[ContextScript] = database.getCollection[ContextScript]("contextScript")

}
