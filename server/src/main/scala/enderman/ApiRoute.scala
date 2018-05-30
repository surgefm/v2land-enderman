package enderman

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import spray.json._

class ApiRoute extends JsonSupport {
  import DefaultJsonProtocol._

  val routes: Route =
    concat(
      path("v2land" / "recent7days") {
        complete(List(1, 2).toJson)
      })

}
