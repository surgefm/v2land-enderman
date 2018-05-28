package enderman.frontend

import com.thoughtworks.binding.{ Binding, dom }
import com.thoughtworks.binding.Binding.{ BindingSeq, Var, Vars }
import org.scalajs.dom.html.Table
import org.scalajs.dom.{ Node, document }

import scala.scalajs.js.annotation.JSExport

object App {

  case class Contact(name: Var[String], email: Var[String])

  val data = Vars.empty[Contact]

  @dom
  def table: Binding[Table] = {
    <table border="1" cellPadding="5">
      <thead>
        <tr>
          <th>Name</th>
          <th>E-mail</th>
        </tr>
      </thead>
      <tbody>
        {
          for (contact <- data) yield {
            <tr>
              <td>
                { contact.name.bind }
              </td>
              <td>
                { contact.email.bind }
              </td>
            </tr>
          }
        }
      </tbody>
    </table>
  }

  @JSExport def main(container: Node) = dom.render(container, table)

}
