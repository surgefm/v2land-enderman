package enderman

import java.util.Date
import java.util.concurrent.TimeUnit

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import enderman.actors.DailyAnalysis
import models.repository

import scala.concurrent.duration._

object QuickstartServer extends App with EnderRoute with Config {

  implicit val system: ActorSystem = ActorSystem("endermanServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  lazy val dailyAnalysisActor = system.actorOf(Props(classOf[DailyAnalysis]), "dailyAnalysis")

  lazy val durationRepo = new repository.DurationRepository(mongo.durationCollection)
  lazy val locationRepo = new repository.LocationRepository(mongo.locationCollection)
  lazy val businessRepo = new repository.BusinessRepository(mongo.businessCollection)
  lazy val contextScriptRepo = new repository.ContextScriptRepository(mongo.contextScriptCollection)

  lazy val routes: Route = enderRoutes

  {
    val millisecondsOfADay = TimeUnit.DAYS.toMillis(1)
    val now = new Date().getTime
    val delay = millisecondsOfADay - (now % millisecondsOfADay)
    system.scheduler.schedule(delay milliseconds, 1 days, dailyAnalysisActor, DailyAnalysis.Tick)
  }

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  println(s"Server online at http://0.0.0.0:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
