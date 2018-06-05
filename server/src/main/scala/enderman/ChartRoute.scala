package enderman

import java.time.{ ZoneId, ZonedDateTime }
import java.util.Date

import akka.http.scaladsl.model.{ HttpEntity, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import enderman.models.Duration

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scalax.chart.api._

object ChartRoute extends JsonSupport {
  import Main.ec
  import util.DateHelper._

  def activeUser(date: Date): Future[Seq[(Int, Int)]] = {
    val sevenDaysAgo = beforeDay(7, date)

    def listOfArr: List[ArrayBuffer[Duration]] = List
      .fill(7)(0)
      .map { _ => ArrayBuffer.empty[Duration] }
    Main
      .durationRepo
      .findBetweenDate(sevenDaysAgo, date)
      .map { locations =>
        (-7 to -1)
          //              TODO: X axis
          //              .map {
          //                offset =>
          //                  yesterday
          //                    .toInstant
          //                    .atZone(ZoneId.of("GMT+8"))
          //                    .minusDays(offset * -1)
          //              }
          .zip {
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
      }
  }

  lazy val routes: Route =
    concat(
      path("v2land" / "activeUser" / "recent7days") {
        val yesterday = yesterdayDate
        onComplete(activeUser(yesterday)) {
          case Success(buf) =>
            val chart = XYLineChart(buf)
            val bytes = chart.encodeAsPNG()
            val entity = HttpEntity(MediaTypes.`image/png`, bytes)
            complete(entity)
          case Failure(e) => {
            e.printStackTrace()
            complete(StatusCodes.BadRequest)
          }
        }
      },
      path("v2land" / "activeUser" / IntNumber / IntNumber / IntNumber) { (year, month, day) =>
        val date = Date.from(ZonedDateTime.of(
          year,
          month,
          day,
          0,
          0,
          0,
          0,
          Config.globalZonedId).toInstant)

        onComplete(activeUser(date)) {
          case Success(buf) =>
            val chart = XYLineChart(buf)
            val bytes = chart.encodeAsPNG()
            val entity = HttpEntity(MediaTypes.`image/png`, bytes)
            complete(entity)
          case Failure(e) => {
            e.printStackTrace()
            complete(StatusCodes.BadRequest)
          }
        }
      })

}
