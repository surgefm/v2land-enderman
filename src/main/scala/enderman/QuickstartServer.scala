package enderman

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import models.repository

object QuickstartServer extends App with EnderRoute {

  implicit val system: ActorSystem = ActorSystem("endermanServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  lazy val durationRepo = new repository.DurationRepository(mongo.durationCollection)
  lazy val locationRepo = new repository.LocationRepository(mongo.locationCollection)
  lazy val businessRepo = new repository.BusinessRepository(mongo.businessCollection)

  lazy val routes: Route = enderRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
