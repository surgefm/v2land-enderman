package enderman.frontend

import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div

object Banner {

  @dom
  def apply: Binding[Div] = {
    <div class="hero is-info is-medium is-bold">
      <div class="hero-body">
        <div class="container has-text-centered">
          <h1 class="title">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit,<br/>
            sed eiusmod tempor incididunt ut labore et dolore magna aliqua
          </h1>
        </div>
      </div>
    </div>
  }

}
