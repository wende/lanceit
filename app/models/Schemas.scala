package models

import org.joda.time.DateTime
import play.api.libs.json.Format
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONObjectID}

// Not unused at all. Do not delete
import play.modules.reactivemongo.json.BSONFormats._


object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val feedDataFormat = Json.format[FeedData]
  implicit val feedFormat = Json.format[FeedItem]
  implicit val userFormat = Json.format[User]
}

case class User(
  username: String ,
  age: Int,
  phoneNumber: Long,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  balance : Option[Double] = Some(0.0),
  feeds : Option[List[BSONObjectID]] = Some(List[BSONObjectID]()),
  activeFeeds : Option[List[BSONObjectID]] = Some(List[BSONObjectID]())
)

case class FeedData
(
  title: String,
  description: String,
  lat: Double,
  lng: Double,
  category: Option[Int],
  cost: Float,
  expireAfter: Long){
  def itemify(username : String) = {
    val time = BSONDateTime(DateTime.now().getMillis)
    val expireAt = BSONDateTime(time.value + expireAfter * 1000 * 60)
    FeedItem(BSONObjectID.generate, username,title,description, time ,lat,lng, category,cost, expireAt)
  }
}

case class FeedItem
(
  _id : BSONObjectID,

  username: String,
  title: String,
  description: String,
  createdAt: BSONDateTime,
  lat: Double,
  lng: Double,
  category: Option[Int],
  cost: Float,
  expireAt: BSONDateTime)

