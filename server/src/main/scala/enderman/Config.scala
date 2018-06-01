package enderman

import com.typesafe.config.ConfigFactory

object Config {

  lazy val config = ConfigFactory.load()

  lazy val isProduction = config.getString("enderman.env") == "production"

}
