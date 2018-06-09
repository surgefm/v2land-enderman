package enderman

import com.typesafe.config.ConfigFactory
import java.time.ZoneId

object Config {

  lazy val config = ConfigFactory.load()

  lazy val isProduction = config.getString("enderman.env") == "production"

  lazy val slackHook = config.getString("enderman.slackHook")

  lazy val pgUri = config.getString("postgresql.uri")

  val globalZonedId = ZoneId.of("GMT+8")

}
