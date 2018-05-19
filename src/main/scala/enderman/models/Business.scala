package enderman.models

import java.util.Date

import org.bson.types.ObjectId

case class Business(
                     _id: ObjectId,
                     action: String,
                     meta: String,
                     sessionId: String,
                     userId: Option[String],
                     date: Date,
                   )
