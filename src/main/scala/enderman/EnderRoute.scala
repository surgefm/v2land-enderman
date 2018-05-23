package enderman

import akka.actor.ActorSystem
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive0, Directive1, Route }
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import enderman.models.{ ContextScript, repository }
import akka.util.{ Timeout }
import java.util.UUID.randomUUID

import akka.http.scaladsl.model.{ HttpHeader, StatusCodes }

import scala.util.{ Failure, Success }
import akka.http.scaladsl.model.headers.{ HttpCookie, HttpCookiePair, RawHeader }
import akka.stream.ActorMaterializer
import org.bson.types.ObjectId
import spray.json.{ JsArray, JsValue, JsonParser, deserializationError }

import scala.concurrent.{ ExecutionContext, Future }

trait EnderRoute extends JsonSupport with Config {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer;
  implicit def ec: ExecutionContext

  lazy val log = Logging(system, classOf[EnderRoute])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  private val sessionIdKey = "sessionId"

  private val optionalSessionCookieDirective: Directive1[Option[HttpCookiePair]] =
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

  private val clientInfoDirective: Directive1[models.ClientInfo] =
    sessionDirective.flatMap { sessionId =>
      extractClientIP.flatMap { clientIp =>
        headerValueByName("User-Agent").map { userAgent =>
          models.ClientInfo(
            clientIp.toOption.map(_.getHostAddress).getOrElse("unknown"),
            userAgent,
            sessionId)
        }
      }
    }

  def durationRepo: repository.DurationRepository
  def locationRepo: repository.LocationRepository
  def businessRepo: repository.BusinessRepository
  def contextScriptRepo: repository.ContextScriptRepository

  lazy val enderRoutes: Route =
    concat(
      pathPrefix("v2land") {
        originHeaderDirective {
          clientInfoDirective { clientInfo =>
            respondWithHeaders(List(
              RawHeader("Access-Control-Allow-Origin", "*"),
              RawHeader("Access-Control-Allow-Credentials", "true"))) {
              concat(
                path("duration") {
                  get {
                    parameters("userId".?, "actionType".as[Int]) { (userIdOpt, actionType) =>
                      val duration = models.Duration(
                        new ObjectId(),
                        actionType,
                        clientInfo.copy(userId = userIdOpt))
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
                        clientInfo.copy(userId = userIdOpt))
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
                },
                path("chunk") {
                  post {
                    entity(as[String]) { jsonString =>
                      val jsonAst = JsonParser(jsonString)
                      jsonAst match {
                        case JsArray(elements: Vector[JsValue]) => {
                          val futures = elements.map { chunk =>
                            val obj = chunk.asJsObject("chunk must be a JsObject")
                            val chunkType = obj.fields("type").toString
                            chunkType match {
                              case "duration" =>
                                durationRepo.insertOne(obj.fields("value").convertTo[models.Duration]);
                              case "location" =>
                                locationRepo.insertOne(obj.fields("value").convertTo[models.Location]);
                              case "business" =>
                                businessRepo.insertOne(obj.fields("value").convertTo[models.Business]);
                            }
                          }
                          val finalFuture = Future.sequence(futures)
                          onComplete(finalFuture) {
                            case Success(_) =>
                              complete("")
                            case Failure(e) => {
                              e.printStackTrace()
                              complete(StatusCodes.BadRequest)
                            }
                          }
                        }
                        case _ => deserializationError("Array expected")
                      }
                    }
                  }
                })
            }
          }
        }
      },
      path("enderpearl.js") {
        respondWithHeader(RawHeader("Content-Type", "application/javascript; charset=UTF-8")) {
          onComplete(contextScriptRepo.latestContent) {
            case Success(content) => complete(content)
            case Failure(e) => {
              e.printStackTrace()
              complete(StatusCodes.BadRequest)
            }
          }
        }
      },
      path("contextscript") {
        post {
          headerValueByName("X-ENDERMAN-TOKEN") { tokenValue =>
            val verifyToken = config.getString("enderman.scriptUploadToken")
            if (tokenValue == verifyToken) {
              fileUpload("bundle") {
                case (_, source) =>
                  val future: Future[String] =
                    source
                      .runFold("") { (acc, n) => acc + n.utf8String }
                      .flatMap({ fileContent =>
                        contextScriptRepo.insertOne(ContextScript(
                          new ObjectId,
                          fileContent))
                      })

                  onComplete(future) {
                    case Success(_) =>
                      complete("")
                    case Failure(e) =>
                      complete("")
                  }
              }
            } else {
              complete(StatusCodes.BadRequest)
            }
          }
        }
      },
      path("") {
        getFromResource("static/index.html")
      }, {
        getFromResourceDirectory("static")
      })

}
