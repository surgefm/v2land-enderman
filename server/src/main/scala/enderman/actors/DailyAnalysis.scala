package enderman.actors

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.event.Logging
import akka.util.Timeout
import enderman.Main
import enderman.util.DateHelper

import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object DailyAnalysis {
  object Tick
}

class DailyAnalysis extends Actor {
  import DailyAnalysis._
  import Main.{ system, ec, durationRepo }

  lazy val log = Logging(system, this)

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  def receive = {
    case Tick =>
      val now = new Date()
      durationRepo.findBetweenDate(DateHelper.yesterdayDate, now) onComplete {
        case Success(durations) =>
          log.info("Begin daily analysis...")
        case Failure(e) =>
          e.printStackTrace()
      }
  }

}
