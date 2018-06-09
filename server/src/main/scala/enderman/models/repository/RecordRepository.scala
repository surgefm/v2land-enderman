package enderman.models.repository

import java.util.Date

import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection

import scala.concurrent.ExecutionContext

object RecordRepository {

  case class RecordAbstract(data: String, createAt: Date)

}

class RecordRepository(
  val pool: ConnectionPool[PostgreSQLConnection])(
  implicit
  ec: ExecutionContext) {
  import RecordRepository._

  def findBetweenDate(
    beginDate: Date,
    endDate: Date = new Date()) = {
    pool.sendPreparedStatement(
      """
        |SELECT "data", "createdAt" FROM record
        | WHERE "action"='createEvent'
        | AND "createdAt" >= ? AND "createdAt" < ?
        | """.stripMargin,
      List(beginDate, endDate))
      .map { queryResult =>
        queryResult.rows match {
          case Some(rows) =>
            rows.map { rowData =>
              RecordAbstract(
                rowData("data").asInstanceOf[String],
                rowData("createAt").asInstanceOf[Date])
            }
          case None =>
            List()
        }
      }
  }

}
