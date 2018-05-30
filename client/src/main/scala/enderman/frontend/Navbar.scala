package enderman.frontend

import com.thoughtworks.binding.{ Binding, dom }
import org.scalajs.dom.html.Div

// <img src="../images/bulma.png" alt="Logo"/>
object Navbar {

  @dom
  def apply: Binding[Div] = {
    <div class="navbar">
      <div class="container">
        <div class="navbar-brand">
          <a class="navbar-item" href="../">
            <h1>Enderman</h1>
          </a>
          <span class="navbar-burger burger">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </div>
        <div id="navbarMenu" class="navbar-menu">
          <div class="navbar-end">
            <a class="navbar-item is-active">
              Home
            </a>
            <a class="navbar-item">
              Examples
            </a>
            <a class="navbar-item">
              Features
            </a>
            <a class="navbar-item">
              Team
            </a>
            <a class="navbar-item">
              Archives
            </a>
            <a class="navbar-item">
              Help
            </a>
            <div class="navbar-item has-dropdown is-hoverable">
              <a class="navbar-link">
                Account
              </a>
              <div class="navbar-dropdown">
                <a class="navbar-item">
                  Dashboard
                </a>
                <a class="navbar-item">
                  Profile
                </a>
                <a class="navbar-item">
                  Settings
                </a>
                <hr class="navbar-divider"/>
                <div class="navbar-item">
                  Logout
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  }

}
