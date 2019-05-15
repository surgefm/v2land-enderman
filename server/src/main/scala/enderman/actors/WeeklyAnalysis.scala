package enderman.actors

import java.util.Date

import akka.actor.Actor
import akka.event.Logging
import enderman.util.SlackHelper
import enderman.util.SlackHelper.{ SlackAttachment, SlackWebHookRequest }
import enderman.Config

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object WeeklyAnalysis {
  object Tick
}

class WeeklyAnalysis extends Actor {
  import WeeklyAnalysis._
  import enderman.Main.{ system, ec }

  lazy val log = Logging(system, this)

  def findMostSubscribedEvent(): Future[(Int, String, Long)] = {
    enderman.database.pg.connectionPool.sendPreparedStatement(
      """
        |SELECT "eventId", name as title, count(subscriber)
        |FROM "subscription", "event"
        |WHERE
        |  "eventId" = "event"."id"
        |GROUP BY "eventId", "event"."name" ORDER BY count DESC
      """.stripMargin
    )
    .map { queryResult =>
      val first = queryResult.rows.get(0)
      (
        first(0).asInstanceOf[Int],
        first(1).asInstanceOf[String],
        first(2).asInstanceOf[Long],
      )
    }
  }

  def sendMostSubscribedEvent() = {

    val promise = for {
      result <- findMostSubscribedEvent()
      msg = List(
          SlackAttachment(
          s"最多用户关注的事件：${result._2}（https://langchao.org/${result._1.toString}）")
      )
    } yield SlackHelper.sendMessage(SlackWebHookRequest("每周推送", msg))

    promise onComplete {
      case Success(_) =>
        log.info("Pushed data to slack")
      case Failure(e) =>
        e.printStackTrace()
    }
  }

  def sendDateRelatedUrl() = {
    val date = new Date().toInstant.atZone(Config.globalZonedId)

    val slackMsg = SlackWebHookRequest(
      "Enderman Weekly",
      List(
        ("上周活跃用户", "activeUser"),
        ("已登录用户占比", "loginUser"),
        ("上周新建事件数量", "createEvent")).map {
          case (text, eventName) =>
            SlackAttachment(
              text,
              Some(
                s"https://enderman.v2land.net/chart/v2land/$eventName/${date.getYear}/${date.getMonthValue}/${date.getDayOfMonth}"
              )
            )
        })

    SlackHelper.sendMessage(slackMsg) onComplete {
      case Success(_) =>
        log.info("Pushed data to slack")
      case Failure(e) =>
        e.printStackTrace()
    }
  }

  def receive = {
    case Tick =>
      sendMostSubscribedEvent()
      sendDateRelatedUrl()
  }

}
