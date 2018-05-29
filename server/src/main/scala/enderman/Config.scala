package enderman

import com.typesafe.config.ConfigFactory

trait Config {

  lazy val config = ConfigFactory.load()

}
