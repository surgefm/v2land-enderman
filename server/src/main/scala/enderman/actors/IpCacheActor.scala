package enderman.actors

import akka.actor.Actor
import akka.event.Logging
import akka.util.Timeout
import enderman.models.IpInfo
import enderman.util.CacheTable

import scala.concurrent.duration._

object IPCacheActor {

  case class GetIpGeolocation(ip: String)

}

class IpCacheActor extends Actor {
  import IPCacheActor._
  import enderman.Main.{ system, ec }

  lazy val log = Logging(system, this)

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  private val cacheTable = new CacheTable[String, IpInfo]

  def receive = {
    case GetIpGeolocation(ip) =>
      sender() ! ip
  }

}

