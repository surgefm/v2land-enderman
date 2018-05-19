package enderman.models

import java.util.Date

import org.bson.types.ObjectId

case class Location (
                      _id: ObjectId, // for mongodb
                      url: String,
                      sessionId: String,
                      userId: Option[String],
                      date: Date,
                    )
