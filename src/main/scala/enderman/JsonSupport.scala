package enderman

import java.util.Date

import enderman.models.{Business, Duration}
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

  implicit object durationJsonFormat extends RootJsonFormat[Duration] {

    def write(duration: Duration) = {
      val obj = JsObject(
        "_id" -> duration._id.toString.toJson,
        "sessionId" -> duration.sessionId.toJson,
        "actionType" -> duration.actionType.toJson,
        "date" -> duration.date.toJson
      )

      duration.userId match {
        case Some(userId) => JsObject(obj.fields.updated("userId", JsString(userId)));
        case _ => obj;
      }
    }

    def read(value: JsValue) = {
      val fields = value.asJsObject.fields
      Duration(
        fields.get("_id") match {
          case Some(id) => new ObjectId(id.convertTo[String]);
          case None => new ObjectId();
        },
        fields("sessionId").convertTo[String],
        fields.get("userId").map(_.convertTo[String]),
        fields("actionType").convertTo[Int],
        fields("date").convertTo[Date]
      )
    }

  }


  implicit object businessJsonFormat extends RootJsonFormat[Business] {

    def write(business: Business) =
      JsObject(
        "action" -> JsString(business.action),
        "meta" -> business.meta.parseJson,
        "sessionId" -> JsString(business.sessionId),
        "date" -> dateJsonFormat.write(business.date),
      )

    def read(value: JsValue) =  {
      val fields = value.asJsObject.fields
      Business(
        fields("action").convertTo[String],
        fields("meta").asJsObject("meta field should be object").convertTo[String],
        fields("sessionId").convertTo[String],
        fields.get("userId").map(_.convertTo[String]),
        fields("date").convertTo[Date]
      )
    }

  }

}
