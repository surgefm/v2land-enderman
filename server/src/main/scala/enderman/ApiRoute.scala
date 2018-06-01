package enderman

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import enderman.models.Duration
import spray.json._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{ Failure, Success }

object ApiRoute extends JsonSupport {
  import DefaultJsonProtocol._
  import Main.ec

  private def dayTime(duration: Int = 1): Long = TimeUnit.DAYS.toMillis(duration)
  private def hourTime(duration: Int = 1): Long = TimeUnit.HOURS.toMillis(duration)

  private def yesterdayDate() = {
    val now = new Date().getTime
    val yesterdayMs = if (Config.isProduction) {
      now - ((now + hourTime(8)) % dayTime())
    } else {
      now - (now % dayTime())
    }
    new Date(yesterdayMs)
  }

  private def beforeDay(num: Int, target: Date) =
    new Date(target.getTime - num * dayTime())

  lazy val routes: Route =
    concat(
      path("v2land" / "recent7days") {
        val yesterday = yesterdayDate
        val sevenDaysAgo = beforeDay(7, yesterday)
        val future: Future[Seq[Int]] = Main
          .locationRepo
          .findBetweenDate(sevenDaysAgo, yesterday)
          .map[Seq[Int]] { locations =>
            locations.foldLeft(ArrayBuffer.fill(7)(0)) { (acc, value) =>
              val createdAt = value.clientInfo.date
              val index = ((createdAt.getTime - sevenDaysAgo.getTime) / dayTime()).toInt
              acc(index) += 1
              acc
            }
          }
        onComplete(future) {
          case Success(buf) =>
            complete(buf)
          case Failure(e) => {
            e.printStackTrace()
            complete(StatusCodes.BadRequest)
          }
        }
      },
      path("v2land" / "activeUser" / "recent7days") {
        val yesterday = yesterdayDate
        val sevenDaysAgo = beforeDay(7, yesterday)

        def listOfArr: List[ArrayBuffer[Duration]] = List
          .fill(7)(0)
          .map { _ => ArrayBuffer.empty[Duration] }

        val future: Future[Seq[Int]] = Main
          .durationRepo
          .findBetweenDate(sevenDaysAgo, yesterday)
          .map[Seq[Int]] { locations =>
            locations
              .foldLeft(listOfArr) { (acc, value) =>
                val createdAt = value.clientInfo.date
                val index = ((createdAt.getTime - sevenDaysAgo.getTime) / dayTime()).toInt
                acc(index).append(value)
                acc
              }
              .map { chunk =>
                chunk
                  .groupBy(_.clientInfo.sessionId)
                  .toList.length
              }
          }
        onComplete(future) {
          case Success(buf) =>
            complete(buf)
          case Failure(e) => {
            e.printStackTrace()
            complete(StatusCodes.BadRequest)
          }
        }
      })

}
