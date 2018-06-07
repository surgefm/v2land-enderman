package enderman.actors

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, HttpResponse, StatusCodes }
import akka.util.{ ByteString, Timeout }
import enderman.JsonSupport
import enderman.models.IpInfo
import enderman.util.CacheTable

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import spray.json._
import DefaultJsonProtocol._

object IpCacheActor {

  case class GetIpGeolocation(ip: String)

}

class IpCacheActor extends Actor with JsonSupport {
  import IpCacheActor._
  import enderman.Main.{ system, ec, materializer }

  lazy val log = Logging(system, this)

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  private val cacheTable = new CacheTable[String, IpInfo]

  def receive = {
    case GetIpGeolocation(ip) =>
      cacheTable.get(ip) match {
        case Some(result) =>
          sender() ! result
        case None =>
          val responseFuture: Future[String] = Http()
            .singleRequest(
              HttpRequest(
                HttpMethods.GET,
                uri = s"http://ip-api.com/json/$ip"))
            .flatMap {
              case HttpResponse(StatusCodes.OK, _, entity, _) =>
                entity.dataBytes.runFold(ByteString(""))(_ ++ _)
            }
            .map(_.utf8String)

          responseFuture onComplete {
            case Success(text) =>
              sender() ! text.parseJson.convertTo[IpInfo]
            case Failure(e) =>
              e.printStackTrace()
          }
      }
  }

}

