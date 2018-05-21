package enderman.models

import org.bson.types.ObjectId

case class Business(
                     _id: ObjectId,
                     action: String,
                     meta: String,
                     clientInfo: ClientInfo,
                   )
