package enderman.frontend

import com.thoughtworks.binding.{ Binding, dom }
import com.thoughtworks.binding.Binding.{ Var, Vars }
import org.scalajs.dom.html.{ Canvas, Div }
import org.scalajs.dom.document
import org.scalajs.dom.experimental.{ Fetch, Response }

import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel, JSGlobal }
import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global

@JSExportTopLevel("Main")
object Main {

  case class Contact(name: Var[String], email: Var[String])

  val data = Vars.empty[Contact]

  @dom
  def app: Binding[Div] = {
    <div class="app">
      { Navbar.apply.bind }
      { Banner.apply.bind }
    </div>
  }

  @js.native
  @JSGlobal("Chart")
  class Chart(ctx: Any, option: js.Object) extends js.Object

  @JSExport def main() = {
    val appDom = document.getElementById("app")

    dom.render(appDom, app)
    renderChart
  }

  private def fetchRecent7Days(): Promise[Response] = {
    Fetch.fetch("/api/v2land/activeUser/recent7days")
  }

  private def renderChart = {
    fetchRecent7Days()
      .toFuture
      .flatMap({ resp => resp.json().toFuture })
      .onComplete {
        case Success(data) => {
          val option = js.Dynamic.literal(
            "type" -> "line",
            "data" -> js.Dynamic.literal(
              "labels" -> js.Array("-7", "-6", "-5", "-4", "-3", "-2", "-1"),
              "datasets" -> js.Array(
                js.Dynamic.literal(
                  "label" -> "浏览用户数量",
                  "data" -> data,
                  "borderWidth" -> 1))))

          val chart7days = document.getElementById("chart-recent7days").asInstanceOf[Canvas]
          val ctx = chart7days.getContext("2d")
          val chart = new Chart(ctx, option)
        }
        case Failure(e) => {
          js.Dynamic.global.console.log(e.asInstanceOf[js.Any])
        }
      }
  }

}
