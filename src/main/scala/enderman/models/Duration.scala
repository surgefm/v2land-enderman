package enderman.models

import org.bson.types.ObjectId


object Action {
  val MouseClick = 1
  val MouseMove = 2
  val KeyDown = 3
  val Scroll = 4
}

case class Duration(
                     _id: ObjectId, // for mongodb
                     actionType: Int,
                     clientInfo: ClientInfo,
                   )
