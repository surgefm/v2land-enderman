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
import akka.http.scaladsl.model._
import enderman.models.repository
import akka.util.{ ByteString, Timeout }

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity.Strict

import scala.util.{ Failure, Success }
import akka.http.scaladsl.model.headers.{ HttpCookiePair, RawHeader }
import akka.stream.ActorMaterializer
import org.bson.types.ObjectId
import spray.json.{ JsArray, JsValue, JsonParser, deserializationError }
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ ExecutionContext, Future }

trait EnderRoute extends JsonSupport {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def ec: ExecutionContext

  lazy val log = Logging(system, classOf[EnderRoute])

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  private lazy val checkOrigin = Config.config.getString("enderman.trackOrigin")

  private val originHeaderDirective: Directive0 =
    optionalHeaderValueByName("Origin").flatMap { value =>
      if (Config.isProduction) {
        value match {
          case Some(headerValue) if headerValue == checkOrigin =>
            pass
          case None =>
            log.error("[production mode origin check] not a request from " + checkOrigin)
            reject
        }
      } else {
        pass
      }
    }

  private val clientInfoDirective: Directive1[models.ClientInfo] =
    for {
      sessionId <- parameter("u")
      clientIp <- extractClientIP
      userAgent <- headerValueByName("User-Agent")
    } yield {
      models.ClientInfo(
        clientIp.toOption.map(_.getHostAddress).getOrElse("unknown"),
        userAgent,
        sessionId)
    }

  def durationRepo: repository.DurationRepository
  def locationRepo: repository.LocationRepository
  def businessRepo: repository.BusinessRepository
  def contextScriptRepo: repository.ContextScriptRepository

  private def decodeBase64(content: String) =
    new String(java.util.Base64.getDecoder.decode(content))

  lazy val enderRoutes: Route =
    concat(
      pathPrefix("v2land") {
        originHeaderDirective {
          respondWithHeaders(List(
            RawHeader("Access-Control-Allow-Origin", "https://langchao.org"),
            RawHeader("Access-Control-Allow-Credentials", "true"),
            RawHeader("Access-Control-Allow-Headers", "Content-Type"),
            RawHeader("Access-Control-Expose-Headers", "*"))) {
            concat(
              clientInfoDirective { clientInfo =>
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
                      parameters("url", "userId".?, "redirectFrom".?, "referrer".?) {
                        (encodedUrl, userIdOpt, redirectFrom, referrer) =>
                          val url = decodeBase64(encodedUrl)
                          val location = models.Location(
                            new ObjectId(),
                            url,
                            redirectFrom.map(decodeBase64),
                            referrer.map(decodeBase64),
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
                  })
              },
              path("chunk") {
                options {
                  complete("")
                } ~
                  post {
                    extractClientIP { clientIp =>
                      headerValueByName("User-Agent") { ua =>
                        entity(as[String]) { jsonString =>
                          import spray.json._

                          val jsonAst = JsonParser(jsonString)
                          val data = jsonAst.asJsObject("root structure must be JsObject")

                          val sid = data.fields("u").toString
                          val userId = data.fields.get("userId").map(_.toString)

                          val clientInfo = models.ClientInfo(
                            clientIp.toOption.map(_.getHostAddress).getOrElse("unknown"),
                            ua,
                            sid,
                            userId)

                          val finalFuture: Future[Seq[String]] = data.fields("content") match {
                            case JsArray(elements: Vector[JsValue]) => {
                              val futures = elements.map {
                                case JsArray(tuple: Vector[JsValue]) =>
                                  val chunkType = tuple(0).convertTo[Int]
                                  val tmp = tuple(1).asJsObject("value muse be JsObject")
                                  val obj = tmp.copy(fields = tmp.fields + ("clientInfo" -> clientInfo.toJson))
                                  chunkType match {
                                    case 0 =>
                                      durationRepo.insertOne(obj.convertTo[models.Duration]);
                                    case 1 =>
                                      locationRepo.insertOne(obj.convertTo[models.Location]);
                                    case 2 =>
                                      businessRepo.insertOne(obj.convertTo[models.Business]);
                                  }
                                case _ =>
                                  Future { deserializationError("Array expected for content") }
                              }
                              Future.sequence(futures)
                            }
                            case _ =>
                              Future { deserializationError("Array expected for content") }
                          }
                          onSuccess(finalFuture) { _ => complete("") }
                        }
                      }
                    }
                  }
              })
          }
        }
      },
      pathPrefix("api") {
        ApiRoute.routes
      },
      pathPrefix("chart") {
        ChartRoute.routes
      },
      path("enderpearl" / Remaining) { filename =>
        val req = Http().singleRequest(
          HttpRequest(uri = s"${Config.staticHost}/js/$filename"))

        onSuccess(req) { resp =>
          complete(resp)
        }
      },
      path("public" / Remaining) { pathString =>
        if (pathString.endsWith(".js") || pathString.endsWith(".map")) {
          getFromResource(pathString)
        } else {
          getFromResource("static/" + pathString)
        }
      },
      path("") {
        complete(HttpResponse(
          200,
          entity = Strict(
            ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`),
            ByteString(Page.index))))
      }, {
        getFromResourceDirectory("static")
      })

}
