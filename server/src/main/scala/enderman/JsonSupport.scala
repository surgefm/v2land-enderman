package enderman

import java.util.Date

import enderman.models._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.bson.types.ObjectId
import spray.json._

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit object dateJsonFormat extends RootJsonFormat[Date] {

    def write(date: Date) = JsNumber(date.getTime())

    def read(value: JsValue) = value match {
      case JsNumber(number) => new Date(number.longValue());
      case _ => deserializationError("Date is an number");
    }

  }

  implicit val ipInfoJsonFormat = jsonFormat14(IpInfo)
  implicit val clientInfoJsonFormat = jsonFormat5(ClientInfo)

  implicit object durationJsonFormat extends RootJsonFormat[Duration] {

    def write(duration: Duration) =
      JsObject(
        "_id" -> duration._id.toString.toJson,
        "actionType" -> duration.actionType.toJson,
        "clientInfo" -> duration.clientInfo.toJson
      )

    def read(value: JsValue) = {
      val fields = value.asJsObject.fields
      Duration(
        fields.get("_id") match {
          case Some(id) => new ObjectId(id.convertTo[String]);
          case None => new ObjectId();
        },
        fields("actionType").convertTo[Int],
        fields("clientInfo").convertTo[ClientInfo],
      )
    }

  }

  implicit object locationJsonFormat extends RootJsonFormat[Location] {

    def write(location: Location) =
      JsObject(
        "_id" -> location._id.toString.toJson,
        "url" -> location.url.toJson,
        "clientInfo" -> location.clientInfo.toJson,
      )

    def read(value: JsValue) = {
      val fields = value.asJsObject.fields
      Location(
        fields.get("_id") match {
          case Some(id) => new ObjectId(id.convertTo[String]);
          case None => new ObjectId();
        },
        fields("url").convertTo[String],
        fields.get("redirectFrom").map { _.convertTo[String] },
        fields.get("referrer").map { _.convertTo[String] },
        fields("clientInfo").convertTo[ClientInfo],
      )
    }

  }


  implicit object businessJsonFormat extends RootJsonFormat[Business] {

    def write(business: Business) =
      JsObject(
        "_id" -> business._id.toString.toJson,
        "action" -> JsString(business.action),
        "meta" -> business.meta.parseJson,
        "clientInfo" -> business.clientInfo.toJson,
      )

    def read(value: JsValue) =  {
      val fields = value.asJsObject.fields
      Business(
        fields.get("_id") match {
          case Some(id) => new ObjectId(id.convertTo[String]);
          case None => new ObjectId();
        },
        fields("action").convertTo[String],
        fields("meta").asJsObject("meta field should be object").convertTo[String],
        fields("clientInfo").convertTo[ClientInfo]
      )
    }

  }

}
