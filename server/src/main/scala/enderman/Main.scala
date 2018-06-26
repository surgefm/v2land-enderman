package enderman

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.Duration
import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import enderman.actors._
import enderman.database.{ mongo, pg }
import enderman.util.DateHelper
import models.repository

import scala.concurrent.duration._

object Main extends App with EnderRoute {

  implicit val system: ActorSystem = ActorSystem("endermanServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val fiveMinutesTaskActor = system.actorOf(Props[FiveMinutesTask], "fiveMinutesTaskActor")
  val dailyAnalysisActor = system.actorOf(Props[DailyAnalysis], "dailyAnalysis")
  val weeklyAnalysisActor = system.actorOf(Props[WeeklyAnalysis], "weeklyAnalysis")
  val ipCacheActor = system.actorOf(Props[IpCacheActor], "ipCacheActor")
  val maskedClientActor = system.actorOf(Props[MaskedClientIdActor], "maskedClientIdActor")

  lazy val durationRepo = new repository.DurationRepository(mongo.durationCollection)
  lazy val locationRepo = new repository.LocationRepository(mongo.locationCollection)
  lazy val businessRepo = new repository.BusinessRepository(mongo.businessCollection)
  lazy val contextScriptRepo = new repository.ContextScriptRepository(mongo.contextScriptCollection)
  lazy val recordRepo = new repository.RecordRepository(pg.connectionPool)
  lazy val maskedClientRepo = new repository.MaskedClientRepository(pg.connectionPool)

  lazy val routes: Route = enderRoutes

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  println(s"Server online at http://0.0.0.0:8080/")

  system.scheduler.schedule(
    DateHelper.duration.delayToTomorrow,
    1 days,
    dailyAnalysisActor,
    DailyAnalysis.Tick)

  system.scheduler.schedule(
    DateHelper.duration.delayToNextWeekMonday9Am,
    7 days,
    weeklyAnalysisActor,
    WeeklyAnalysis.Tick)

  system.scheduler.schedule(
    DateHelper.duration.delayToNextFiveMinutes,
    5 minutes,
    fiveMinutesTaskActor,
    FiveMinutesTask.Tick)

  Await.result(system.whenTerminated, Duration.Inf)

}
