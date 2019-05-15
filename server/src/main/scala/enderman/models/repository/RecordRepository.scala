package enderman.models.repository

import java.util.Date

import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import org.joda.time.DateTime

import scala.concurrent.{ ExecutionContext, Future }

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
    endDate: Date = new Date()): Future[Seq[RecordRepository.RecordAbstract]] = {

    pool.sendPreparedStatement(
      """
        |SELECT "data", "createdAt" FROM record
        | WHERE "action"='createEvent'
        | AND "createdAt" >= ? AND "createdAt" < ?
        | """.stripMargin,
      List(beginDate, endDate))
      .map { queryResult =>
        queryResult.rows.getOrElse(List()).map { rowData =>
          RecordAbstract(
            rowData(0).asInstanceOf[String],
            rowData(1).asInstanceOf[DateTime].toDate)
        }
      }
  }

}
