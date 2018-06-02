package enderman

import java.time.{ ZoneId, ZonedDateTime }

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

  lazy val routes: Route =
    concat(
      path("v2land" / "activeUser" / "recent7days") {
        val yesterday = yesterdayDate
        val sevenDaysAgo = beforeDay(7, yesterday)

        def listOfArr: List[ArrayBuffer[Duration]] = List
          .fill(7)(0)
          .map { _ => ArrayBuffer.empty[Duration] }
        val future: Future[Seq[(Int, Int)]] = Main
          .durationRepo
          .findBetweenDate(sevenDaysAgo, yesterday)
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
        onComplete(future) {
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
