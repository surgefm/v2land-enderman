package enderman.models

import java.util.Date

case class Location (
                      url: String,
                      sessionId: String,
                      userId: Option[String],
                      date: Date,
                    )
