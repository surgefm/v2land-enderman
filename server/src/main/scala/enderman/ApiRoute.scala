package enderman

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import spray.json._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{ Failure, Success }

object ApiRoute extends JsonSupport {
  import DefaultJsonProtocol._
  import Main.ec

  private lazy val millisecondsOfADay: Long = TimeUnit.DAYS.toMillis(1)

  private def yesterdayDate() = {
    val now = new Date().getTime
    val yesterdayMs = now - (now % millisecondsOfADay)
    new Date(yesterdayMs)
  }

  private def beforeDay(num: Int, target: Date) =
    new Date(target.getTime - num * millisecondsOfADay)

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
              val index = ((createdAt.getTime - sevenDaysAgo.getTime) / millisecondsOfADay).toInt
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
      })

}
