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

  def findMostSubscribedEvent(): Future[Seq[(Int, String, Long)]] = {
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
      queryResult.rows.get.slice(0, 3).map { item =>
        (
          item(0).asInstanceOf[Int],
          item(1).asInstanceOf[String],
          item(2).asInstanceOf[Long],
        )
      }
    }
  }

  def sendMostSubscribedEvent() = {

    val promise = for {
      result <- findMostSubscribedEvent()
      msg = result.toList.map { item =>
        SlackAttachment(
          s"${item._2}（${item._3.toString}）：https://langchao.org/${item._1.toString}"
        )
      }
    } yield SlackHelper.sendMessage(SlackWebHookRequest("用户关注最多事件", msg))

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

  def sendMostViewedUrls(): Unit = {
    val result = for {
      urls <- enderman.Main.locationRepo.findMostViewedLocations
      msg = urls.toList.map { item => SlackAttachment(s"${item._1} (${item._2})") }
    } yield SlackHelper.sendMessage(SlackWebHookRequest("用户访问最多的链接", msg))

    result onComplete {
      case Success(_) =>
        log.info("Pushed data to slack")
      case Failure(e) =>
        e.printStackTrace()
    }
  }

  def receive = {
    case Tick =>
      sendDateRelatedUrl()
      sendMostSubscribedEvent()
      sendMostViewedUrls()
  }

}
