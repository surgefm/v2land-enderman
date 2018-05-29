package enderman.models

import java.util.Date

case class ClientInfo(
                        clientIp: String,
                        userAgent: String,
                        sessionId: String,
                        userId: Option[String] = None,
                        date: Date = new Date(),
                        ) {

  def withDateOffset(offset: Long) =
    this.copy(date = new Date(date.getTime + offset))

  def withUserId(userId: String) =
    this.copy(userId = Some(userId))

}
