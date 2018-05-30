package enderman.frontend

import com.thoughtworks.binding.{ Binding, dom }
import com.thoughtworks.binding.Binding.{ BindingSeq, Var, Vars }
import org.scalajs.dom.html.Div
import org.scalajs.dom.{ Node, document }

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("Main")
object Main {

  case class Contact(name: Var[String], email: Var[String])

  val data = Vars.empty[Contact]

  @dom
  def app: Binding[Div] = {
    <div class="app">
      { Navbar.apply.bind }
      { Banner.apply.bind }
      { Body.apply.bind }
    </div>
  }

  @JSExport def main() =
    dom.render(document.getElementById("app"), app)

}
