package enderman.actors

import java.time.ZonedDateTime
import java.util.Date

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import enderman.{Config, JsonSupport}
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object WeeklyAnalysis extends JsonSupport {
  import DefaultJsonProtocol._
  object Tick

  case class SlackWebHookRequest(
    text: String,
    attachments: List[SlackImageAttachment])

  case class SlackImageAttachment(
    title: String,
    image_url: String)

  implicit val slackImageAttachmentJsonFormat = jsonFormat2(SlackImageAttachment)
  implicit val slackWebHookRequestJsonFormat = jsonFormat2(SlackWebHookRequest)
}

class WeeklyAnalysis extends Actor {
  import WeeklyAnalysis._
  import enderman.Main.{ system, ec, durationRepo }

  lazy val log = Logging(system, this)

  def receive = {
    case Tick =>
      val date = new Date().toInstant.atZone(Config.globalZonedId)
      val slackMsg = SlackWebHookRequest(
        "Enderman Weekly",
        List(SlackImageAttachment(
          "上周活跃用户",
          s"https://enderman.v2land.net/chart/v2land/activeUser/${date.getYear}/${date.getMonthValue}/${date.getDayOfMonth}")))
      val responseFuture: Future[HttpResponse] = Http()
        .singleRequest(
          HttpRequest(
            HttpMethods.POST,
            uri = Config.slackHook,
            entity = HttpEntity(
              MediaTypes.`application/json`,
              ByteString(slackMsg.toJson.compactPrint))))

      responseFuture onComplete {
        case Success(_) =>
          log.info("Pushed data to slack")
        case Failure(e) =>
          e.printStackTrace()
      }
  }

}
