package enderman.models

import java.util.Date

import org.mongodb.scala.bson.ObjectId

case class ContextScript(
  _id: ObjectId,
  content: String,
  date: Date = new Date)
