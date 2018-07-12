package enderman.database

import java.util.Date

import enderman.{ Config, JsonSupport }
import enderman.models._
import org.bson.{ BsonReader, BsonType, BsonWriter, json }
import org.bson.codecs.{ Codec, DecoderContext, DocumentCodec, EncoderContext }
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object mongo {

  object BusinessCodec extends Codec[Business] with JsonSupport {

    private lazy val clientInfoCodec: Codec[ClientInfo] =
      codecRegistry.get(classOf[ClientInfo])

    private lazy val documentCodec: Codec[bson.Document] =
      codecRegistry.get(classOf[bson.Document])

    override def encode(writer: BsonWriter, value: Business, encoderContext: EncoderContext): Unit = {

      writer.writeStartDocument()
      writer.writeName("_id")
      writer.writeObjectId(value._id)
      writer.writeName("action")
      writer.writeString(value.action)
      writer.writeName("meta")
      writer.pipe(new json.JsonReader(value.meta))
      writer.writeName("clientInfo")

      clientInfoCodec.encode(writer, value.clientInfo, encoderContext)

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
      val metaDoc = documentCodec.decode(reader, decoderContext)
      reader.readName("clientInfo")
      val clientInfo = clientInfoCodec.decode(reader, decoderContext)
      reader.readEndDocument()

      Business(
        _id,
        action,
        metaDoc.toString(),
        clientInfo)
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
