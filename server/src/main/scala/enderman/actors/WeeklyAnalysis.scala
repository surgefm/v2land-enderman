package enderman.actors

import java.util.Date

import akka.actor.Actor
import akka.event.Logging
import enderman.util.SlackHelper
import enderman.util.SlackHelper.{ SlackImageAttachment, SlackWebHookRequest }
import enderman.Config

import scala.util.{ Failure, Success }

object WeeklyAnalysis {
  object Tick
}

class WeeklyAnalysis extends Actor {
  import WeeklyAnalysis._
  import enderman.Main.{ system, ec }

  lazy val log = Logging(system, this)

  def receive = {
    case Tick =>
      val date = new Date().toInstant.atZone(Config.globalZonedId)
      val slackMsg = SlackWebHookRequest(
        "Enderman Weekly",
        List(SlackImageAttachment(
          "上周活跃用户",
          s"https://enderman.v2land.net/chart/v2land/activeUser/${date.getYear}/${date.getMonthValue}/${date.getDayOfMonth}")))

      SlackHelper.sendMessage(slackMsg) onComplete {
        case Success(_) =>
          log.info("Pushed data to slack")
        case Failure(e) =>
          e.printStackTrace()
      }
  }

}
