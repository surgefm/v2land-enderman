package enderman

import java.time.{ ZoneId, ZonedDateTime }
import java.util.Date

import akka.http.scaladsl.model.{ HttpEntity, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import enderman.models.Duration
import enderman.models.repository.RecordRepository
import enderman.util.LRUMap

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scalax.chart.api._

object ChartRoute extends JsonSupport {
  import Main.ec
  import util.DateHelper._

  private def activeUser(date: Date, duration: Int = 7): Future[Seq[(Int, Int)]] = {
    val sevenDaysAgo = beforeDay(duration, date)

    def listOfArr: List[ArrayBuffer[Duration]] = List
      .fill(duration)(0)
      .map { _ => ArrayBuffer.empty[Duration] }
    Main
      .durationRepo
      .findBetweenDate(sevenDaysAgo, date)
      .map { locations =>
        ((-1 * duration) to -1)
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

  private def createEvent(date: Date, duration: Int = 7): Future[Seq[(Int, Int)]] = {
    val sevenDaysAgo = beforeDay(duration, date)
    def listOfArr: List[ArrayBuffer[RecordRepository.RecordAbstract]] = List
      .fill(duration)(0)
      .map { _ => ArrayBuffer.empty[RecordRepository.RecordAbstract] }

    Main
      .recordRepo
      .findBetweenDate(sevenDaysAgo, date)
      .map { recordAbstracts =>
        ((-1 * duration) to -1)
          .zip {
            recordAbstracts
              .foldLeft(listOfArr) { (acc, value) =>
                val createdAt = value.createAt
                val index = ((createdAt.getTime - sevenDaysAgo.getTime) / dayTime()).toInt
                acc(index).append(value)
                acc
              }
              .map { _.length }
          }

      }

  }

  private def loginUser(date: Date, duration: Int = 7): Future[Seq[(Int, Double)]] = {
    val sevenDaysAgo = beforeDay(duration, date)

    def listOfArr: List[ArrayBuffer[Duration]] = List
      .fill(duration)(0)
      .map { _ => ArrayBuffer.empty[Duration] }

    Main
      .durationRepo
      .findBetweenDate(sevenDaysAgo, date)
      .map { locations =>
        ((-1 * duration) to -1)
          .zip {
            locations
              .foldLeft(listOfArr) { (acc, value) =>
                val createdAt = value.clientInfo.date
                val index = ((createdAt.getTime - sevenDaysAgo.getTime) / dayTime()).toInt
                acc(index).append(value)
                acc
              }
              .map { chunk =>
                val tuples = chunk
                  .groupBy(_.clientInfo.sessionId)
                  .toList
                  .map {
                    case (sessionId, durations) =>
                      (sessionId, durations.exists(_.clientInfo.userId.isDefined))
                  }

                val children = tuples.filter {
                  case (sessionId, hasUserId) => hasUserId
                }

                children.length * 1.0 / tuples.length
              }
          }
      }
  }

  private val recent30DaysActiveUserCache = new LRUMap[Long, Array[Byte]](8)
  private val recent30DaysCreateEventCache = new LRUMap[Long, Array[Byte]](8)

  lazy val routes: Route =
    concat(
      path("v2land" / "activeUser" / "recent7days") {
        val yesterday = yesterdayDate

        val bytesFuture = for {
          buf <- activeUser(yesterday)
          chart = XYLineChart(buf)
        } yield chart.encodeAsPNG()

        onSuccess(bytesFuture) { bytes =>
          val entity = HttpEntity(MediaTypes.`image/png`, bytes)
          complete(entity)
        }
      },
      path("v2land" / "activeUser" / "recent30days") {
        val yesterday = yesterdayDate

        recent30DaysActiveUserCache.get(yesterday.getTime) match {
          case Some(bytes) =>
            val entity = HttpEntity(MediaTypes.`image/png`, bytes)
            complete(entity)
          case None =>
            val bytesFuture = for {
              buf <- activeUser(yesterday, 30)
              chart = XYLineChart(buf)
            } yield chart.encodeAsPNG()

            onSuccess(bytesFuture) { bytes =>
              recent30DaysActiveUserCache.put(yesterday.getTime, bytes)
              val entity = HttpEntity(MediaTypes.`image/png`, bytes)
              complete(entity)
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

        recent30DaysActiveUserCache.get(date.getTime) match {
          case Some(bytes) =>
            val entity = HttpEntity(MediaTypes.`image/png`, bytes)
            complete(entity)
          case None =>
            val bytesFuture = for {
              buf <- activeUser(date, 30)
              chart = XYLineChart(buf)
            } yield chart.encodeAsPNG()

            onSuccess(bytesFuture) { bytes =>
              recent30DaysActiveUserCache.put(date.getTime, bytes)
              val entity = HttpEntity(MediaTypes.`image/png`, bytes)
              complete(entity)
            }
        }
      },
      path("v2land" / "createEvent" / IntNumber / IntNumber / IntNumber) { (year, month, day) =>
        val date = Date.from(ZonedDateTime.of(
          year,
          month,
          day,
          0,
          0,
          0,
          0,
          Config.globalZonedId).toInstant)

        recent30DaysCreateEventCache.get(date.getTime) match {
          case Some(bytes) =>
            val entity = HttpEntity(MediaTypes.`image/png`, bytes)
            complete(entity)
          case None =>
            val bytesFuture = for {
              buf <- createEvent(date, 30)
              chart = XYLineChart(buf)
            } yield chart.encodeAsPNG()

            onSuccess(bytesFuture) { bytes =>
              recent30DaysCreateEventCache.put(date.getTime, bytes)
              val entity = HttpEntity(MediaTypes.`image/png`, bytes)
              complete(entity)
            }
        }
      },
      path("v2land" / "loginUser" / IntNumber / IntNumber / IntNumber) { (year, month, day) =>
        val date = Date.from(ZonedDateTime.of(
          year,
          month,
          day,
          0,
          0,
          0,
          0,
          Config.globalZonedId).toInstant)

        val bytesFuture = for {
          buf <- loginUser(date, 30)
          chart = XYLineChart(buf)
        } yield chart.encodeAsPNG()

        onSuccess(bytesFuture) { bytes =>
          val entity = HttpEntity(MediaTypes.`image/png`, bytes)
          complete(entity)
        }
      })

}
