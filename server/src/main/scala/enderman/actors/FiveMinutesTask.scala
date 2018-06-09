package enderman.actors

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.util.ByteString
import enderman.Config
import enderman.util.SlackHelper
import enderman.util.SlackHelper.SlackWebHookRequest
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

import scala.util.{Failure, Success}

object FiveMinutesTask {

  object Tick

}

class FiveMinutesTask extends Actor {
  import FiveMinutesTask._
  import enderman.Main.{ system, ec, materializer }

  lazy val log = Logging(system, this)

  def receive = {
    case Tick =>
      if (Config.isProduction) {
        val browser = JsoupBrowser()

        val req = Http()
          .singleRequest(
            HttpRequest(
              HttpMethods.GET,
              uri = "https://langchao.org/"))

        val htmlStrFuture = for {
          response <- req
          textContentBytes <- response match {
            case HttpResponse(StatusCodes.OK, _, entity, _) =>
              entity.dataBytes.runFold(ByteString(""))(_ ++ _)
          }
        } yield textContentBytes.utf8String

        def reportError(throwable: Throwable) = {
          val slackMsg = SlackWebHookRequest(
            "浪潮首页显示有问题哦，赶快去看看吧",
            List())

          SlackHelper.sendMessage(slackMsg) onComplete {
            case Success(_) =>
              log.error(throwable.toString)
            case Failure(e) =>
              e.printStackTrace()
          }
        }

        htmlStrFuture onComplete {
          case Success(htmlStr) =>
            try {
              val homepage = browser.parseString(htmlStr)
              val items = homepage >> elementList("div.event-text.event-text-image")
              if (items.isEmpty) {
                reportError(new Exception("event list is less then zero"))
              } else {
                log.info("homepage check successful")
              }
            } catch {
              case ex: Exception =>
                ex.printStackTrace()
                reportError(ex)
            }
          case Failure(e) =>
            e.printStackTrace()
            reportError(e)
        }
      }
  }

}
