package enderman.util

import enderman.{ Config, JsonSupport }
import spray.json._
import DefaultJsonProtocol._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, HttpMethods, HttpRequest, MediaTypes }
import akka.util.ByteString

object SlackHelper extends JsonSupport {

  case class SlackWebHookRequest(
    text: String,
    attachments: List[SlackAttachment])

  case class SlackAttachment(
    title: String,
    image_url: Option[String] = None)

  implicit lazy val slackAttachmentJsonFormat =
    jsonFormat2(SlackAttachment)

  implicit lazy val slackWebHookRequestJsonFormat =
    jsonFormat2(SlackWebHookRequest)

  def sendMessage(req: SlackWebHookRequest)(implicit system: ActorSystem) = {
    Http()
      .singleRequest(
        HttpRequest(
          HttpMethods.POST,
          uri = Config.slackHook,
          entity = HttpEntity(
            MediaTypes.`application/json`,
            ByteString(req.toJson.compactPrint))))
  }

}
