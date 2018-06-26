package enderman.actors

import akka.actor.Actor
import scala.util.{ Failure, Success }
import akka.event.Logging
import akka.util.Timeout
import enderman.util.CacheTable
import enderman.models.MaskedClient
import enderman.Main

import scala.concurrent.duration._

object MaskedClientIdActor {

  case class GetMaskedClientId(realId: Int)

}

class MaskedClientIdActor extends Actor {
  import MaskedClientIdActor._
  import enderman.Main.{ system, ec, materializer }

  lazy val log = Logging(system, this)

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  private val cacheTable = new CacheTable[Int, MaskedClient]

  def receive = {
    case GetMaskedClientId(realId) =>
      val s = sender()
      cacheTable.get(realId) match {
        case Some(maskedClient) =>
          s ! maskedClient
        case None =>
          val mcFuture = for {
            maskedId <- Main.maskedClientRepo.findMaskedId(realId)
            maskedClient = MaskedClient(maskedId)
            _ = cacheTable.put(realId, maskedClient)
          } yield maskedClient

          mcFuture onComplete {
            case Success(mc) =>
              s ! mc
            case Failure(e) =>
              e.printStackTrace()
          }
      }
  }

}
