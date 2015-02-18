package models

import org.joda.time.DateTime
import play.api.libs.json.Format
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONObjectID}
import services.helpers.Helpers

import scala.util.Try

// Not unused at all. Do not delete
import play.modules.reactivemongo.json.BSONFormats._


object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros

  implicit val geoFormat  = Json.format[GeoBSON]
  implicit val feedDataFormat = Json.format[FeedData]
  implicit val feedFormat = Json.format[FeedItem]
  implicit val userFormat = Json.format[User]
}

case class User(
  username: String ,
  phoneNumber: Option[Long],
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  balance : Option[Double] = Some(0.0),
  feeds : Option[List[BSONObjectID]] = Some(List[BSONObjectID]()),
  activeFeeds : Option[List[BSONObjectID]] = Some(List[BSONObjectID]()),
  shareholders: List[String] = List(),
  titles : List[String] = List()
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
    FeedItem(
      BSONObjectID.generate,
      username,
      title,
      description,
      Point(lat, lng).toBSON,
      category,
      cost,
      time,
      expireAt)
  }
}

case class FeedItem
(
  _id : BSONObjectID,

  username: String,
  title: String,
  description: String,
  loc: GeoBSON,
  category: Option[Int],
  cost: Float,
  createdAt: BSONDateTime,
  expireAt: BSONDateTime,
  stage: Int = 0,
  completed: Boolean = false)

case class Point(lat: Double, lng : Double){
  lazy val toBSON = GeoBSON("Point", List(lat, lng))
}
case class GeoBSON(`type` : String, coordinates: List[Double] )


// ========================================================
//================= PROMOTIONAL CODES =====================
// ========================================================

case class Code(code: String) {
  lazy val title      = code.substring(0,3)
  lazy val id         = code.substring(3,3+Code.idLength)
  lazy val checksum   = code.substring(3+Code.idLength)
}
object Code {
  lazy val idLength = BSONObjectID.generate.stringify.length
  def parse(string: String) = {
    Try {
      val title = Title(string.substring(0,3)).get
      val id = BSONObjectID(string.substring(3, 3 + idLength ).reverse).stringify
      assert (Helpers.adler32sum(title.code + id.reverse).toString == string.substring(3 + idLength), {
        println(string)
        println(title.code)
        println(id.reverse)
        println(Helpers.adler32sum(title.code+id.reverse))
      })
      title
    }
  }
  def generate(titleCode: String)  = Try[Code]{
    val title = Title(titleCode).get
    val code = title.code + BSONObjectID.generate.stringify.reverse
    Code(code + Helpers.adler32sum(code).toString)
  }
}

trait Benefit
case class Discount() extends Benefit
abstract class Title(val code : String, val name : String) {
  val benefits : List[Benefit] = List()
}
object Title {
  def apply(code : String ) ={
    Option[Title](code match {
      case "VIP" => VIP()
      case _ => null
    })
  }

}
case class VIP() extends Title("VIP", "VIP") {
  override val benefits = List()
}