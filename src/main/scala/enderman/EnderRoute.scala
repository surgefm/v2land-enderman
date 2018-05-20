package enderman

import java.util.Date

import akka.actor.ActorSystem
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive0, Directive1, Route }
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import enderman.models.repository
import akka.util.Timeout
import java.util.UUID.randomUUID

import akka.http.scaladsl.model.StatusCodes

import scala.util.{ Failure, Success }
import akka.http.scaladsl.model.headers.{ HttpCookie, HttpCookiePair }
import org.bson.types.ObjectId

trait EnderRoute extends JsonSupport with Config {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[EnderRoute])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val sessionIdKey = "sessionId"

  val optionalSessionCookieDirective: Directive1[Option[HttpCookiePair]] =
    optionalCookie(sessionIdKey)

  // check the existence sessionId
  // generate new sessionId if not exist
  private val sessionDirective: Directive1[String] =
    optionalSessionCookieDirective.flatMap {
      case Some(cookie) => provide(cookie.value);
      case None => {
        val newId = randomUUID().toString

        setCookie(HttpCookie(sessionIdKey, newId)).tmap(_ => newId)
      };
    }

  private lazy val checkOrigin = config.getString("enderman.trackOrigin")

  private val originHeaderDirective: Directive0 =
    headerValueByName("Origin").flatMap { value =>
      if (value == checkOrigin) {
        pass
      } else {
        log.error("not a request from " + checkOrigin)
        reject
      }
    }

  def durationRepo: repository.DurationRepository
  def locationRepo: repository.LocationRepository
  def businessRepo: repository.BusinessRepository

  lazy val enderRoutes: Route =
    path("") {
      complete("God's in his heaven.")
    } ~
      pathPrefix("v2land") {
        originHeaderDirective {
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
                  parameters("url", "userId".?) { (url, userIdOpt) =>
                    val location = models.Location(
                      new ObjectId(),
                      url,
                      sessionId,
                      userIdOpt,
                      new Date())
                    onComplete(locationRepo.insertOne(location)) {
                      case Success(_) => complete("")
                      case Failure(e) => {
                        e.printStackTrace()
                        complete(StatusCodes.BadRequest)
                      }
                    }
                  }
                }
              },
              path("business") {
                post {
                  entity(as[models.Business]) { business =>
                    onComplete(businessRepo.insertOne(business)) {
                      case Success(_) => complete("")
                      case Failure(e) => {
                        e.printStackTrace()
                        complete(StatusCodes.BadRequest)
                      }
                    }
                  }
                }
              })

          }
        }
      }

}
