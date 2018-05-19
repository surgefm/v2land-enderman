package enderman

import java.util.Date

import akka.actor.{ ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive1, Route }
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import enderman.models.repository
import akka.util.Timeout
import java.util.UUID.randomUUID

import akka.http.scaladsl.model.{ StatusCodes }

import scala.util.{ Failure, Success }
import akka.http.scaladsl.model.headers.{ HttpCookie, HttpCookiePair }
import org.bson.types.ObjectId

trait EnderRoute extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[EnderRoute])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val sessionIdKey = "sessionId"

  val optionalSessionCookieDirective: Directive1[Option[HttpCookiePair]] =
    optionalCookie(sessionIdKey)

  // check the existence sessionId
  // generate new sessionId if not exist
  val sessionDirective: Directive1[String] =
    optionalSessionCookieDirective.flatMap {
      case Some(cookie) => provide(cookie.value);
      case None => {
        val newId = randomUUID().toString

        setCookie(HttpCookie(sessionIdKey, newId)).tmap(_ => newId)
      };
    }

  def durationRepo: repository.DurationRepository

  lazy val enderRoutes: Route =
    pathPrefix("v2land") {
      sessionDirective { sessionId =>
        concat(
          path("duration") {
            get {
              parameters("userId".?, "actionType".as[Int]) { (userIdOpt, actionType) =>
                val duration = models.Duration(
                  new ObjectId(),
                  sessionId,
                  userIdOpt,
                  actionType,
                  new Date())
                onComplete(durationRepo.insertOne(duration)) {
                  case Success(_) => complete("")
                  case Failure(e) => {
                    e.printStackTrace()
                    complete(StatusCodes.BadRequest)
                  }
                }
              }
            }
          },
          path("location") {
            get {
              parameters("url", "userId".?) {
                (url, userIdOpt) =>
                  complete(sessionId)
              }
            }
          },
          path(Segment) { eventName =>
            concat(
              post {
                complete(sessionId)
              })
          })

      }
    }

}
