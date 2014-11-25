package models

import play.api.libs.json.Format
import reactivemongo.bson.{BSONDateTime, BSONObjectID}

// Not unused at all. Do not delete
import play.modules.reactivemongo.json.BSONFormats._


object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val feedFormat = Json.format[FeedItem]
  implicit val userFormat = Json.format[User]
}

case class User(
                _id : BSONObjectID,
                 age: Int,
                 firstName: String,
                 lastName: String)

case class FeedItem
(
  _id : Option[BSONObjectID],
  username: String,
  title: String,
  description: String,
  timestamp: Long,
  lat: Double,
  lng: Double,
  expireAt: BSONDateTime)

