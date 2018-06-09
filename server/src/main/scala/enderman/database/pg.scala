package enderman.database

import com.github.mauricio.async.db.pool.{ ConnectionPool, PoolConfiguration }
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser
import enderman.Config
import com.github.mauricio.async.db.postgresql.pool.PostgreSQLConnectionFactory

object pg {

  lazy val configuration = URLParser.parse(Config.pgUri)

  lazy val connectionFactory = new PostgreSQLConnectionFactory(configuration)

  lazy val connectionPool = new ConnectionPool[PostgreSQLConnection](connectionFactory, PoolConfiguration.Default)

}
