package enderman.actors

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.event.Logging
import akka.util.Timeout
import enderman.Main

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._

import scala.util.{ Failure, Success }

object DailyAnalysis {
  case class Tick()
}

class DailyAnalysis extends Actor {
  import DailyAnalysis._
  import Main._

  lazy val log = Logging(system, classOf[DailyAnalysis])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  private lazy val millisecondsOfADay = TimeUnit.DAYS.toMillis(1)

  def getYesterday(today: Date = new Date) =
    new Date(today.getTime - millisecondsOfADay)

  def receive = {
    case Tick =>
      val now = new Date()
      onComplete(durationRepo.findBetweenDate(getYesterday(now), now)) {
        case Success(durations) =>
          log.info("Begin daily analysis...")
          complete("")
        case Failure(e) =>
          e.printStackTrace()
          complete("")
      }
  }

}
