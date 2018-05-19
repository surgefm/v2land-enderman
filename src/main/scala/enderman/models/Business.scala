package enderman.models

import java.util.Date

case class Business(
                   action: String,
                   meta: String,
                   sessionId: String,
                   userId: Option[String],
                   date: Date,
                   )
