package enderman.models.repository

import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import java.util.UUID.randomUUID
import java.util.Base64
import java.nio.charset.StandardCharsets

import java.security.MessageDigest
import java.math.BigInteger

import scala.concurrent.{ ExecutionContext, Future }

class MaskedClientRepository(
  val pool: ConnectionPool[PostgreSQLConnection])(
  implicit
  ec: ExecutionContext) {

  val initSQL =
    """
      |CREATE TABLE IF NOT EXISTS "public"."masked_client" (
      |  "real_id" int4 NOT NULL,
      |  "masking_id" text NOT NULL,
      |  PRIMARY KEY ("real_id")
      |)
      |;
    """.stripMargin

  private var inited = false

  def init = {
    if (inited) {
      Future()
    } else {
      pool.sendQuery(initSQL).map { _ =>
        inited = true
      }
    }
  }

  def findMaskedId(realId: Int) =
    for {
      _ <- init
      queryResult <- pool.sendPreparedStatement(
        """
          |SELECT "masking_id" FROM masked_client
          | WHERE "real_id" = ?
        """.stripMargin,
        List(realId))
      id <- queryResult.rows match {
        case Some(rows) =>
          if (rows.nonEmpty) {
            val rowData = rows(0)
            Future(rowData(0).asInstanceOf[String])
          } else {
            createMaskedId(realId)
          }
        case None => createMaskedId(realId)
      }
    } yield id

  def userExist(realId: Int): Future[Boolean] =
    for {
      queryResult <- pool.sendPreparedStatement(
        """
          |SELECT "id" FROM client
          | WHERE "id" = ?
        """.stripMargin,
        List(realId))
      result = queryResult.rows match {
        case Some(rows) =>
          if (rows.isEmpty) false
          else true
        case None => false
      }
    } yield result

  def createMaskedId(realId: Int) = {

    userExist(realId).flatMap { isUserExist =>
      if (isUserExist) {
        val uuid = randomUUID().toString
        val crypt = MessageDigest.getInstance("SHA-1")
        crypt.reset()
        crypt.update(uuid.getBytes("UTF-8"))

        val maskedId = new BigInteger(1, crypt.digest).toString(16).slice(0, 8)

        pool.sendPreparedStatement(
          """
            |INSERT INTO "public"."masked_client"("real_id", "masking_id")
            | VALUES (?, ?)
            | ;
          """.stripMargin,
          List(realId, maskedId)).map { _ => maskedId }
      } else {
        Future.failed(new Error("User no exist"))
      }
    }

  }

}
