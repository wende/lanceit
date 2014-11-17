package services

import reactivemongo.bson.BSONObjectID


object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val feedFormat = Json.format[Feed]
  implicit val userFormat = Json.format[User]
}

case class User(
                _id : BSONObjectID,
                 age: Int,
                 firstName: String,
                 lastName: String,
                 feeds: List[Feed])

case class Feed(
                 name: String,
                 url: String)

