package controllers

import play.api.libs.json.Json.JsValueWrapper
import services.helpers.{Helpers, Memoize}
import models.{Attendant, FeedData, FeedItem}
import play.api.mvc._
import play.api.libs.json._
import models.JsonFormats._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._
import play.api.Play.current
import services.database._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration._
import services.helpers.Helpers.$
import services.helpers.Helpers.$arr
import Application.authorized
import async.Async._
object FeedListController extends Controller{

  val maxDistance = 20000;
  def near(lat: Double, lng: Double, max: Double) =
    $("$near" -> $("type" -> "Point", "coordinates" -> $arr(lat,lng)), "$maxDistance" -> max)

  def get = Action.async {
    val selector = $("expireAt" -> $("$gt" -> BSONDateTime(Helpers.now)))
    Database.feeds.find(selector).cursor[FeedItem].collect[List]().map {
      feeds => Ok($("feeds" -> feeds,  "timestamp" -> Helpers.now))
    }
  }
  def getByLatLng(lat : Double, lng : Double, max: Double = maxDistance) = Action.async {
    val selector = $("expireAt" -> $("$gt" -> BSONDateTime(Helpers.now)), "loc" -> near(lat,lng,max))
    Database.feeds.find(selector).cursor[FeedItem].collect[List]().map {
      feeds => Ok($("feeds" -> feeds, "timestamp" -> Helpers.now))
    } fallbackTo Future.successful(InternalServerError)
  }
  def getByLatLngMax(lat: Double, lng: Double, max: Double) = {
    getByLatLng(lat, lng, max)
  }

  def getById(id: String) = Action.async {
    BSONObjectID.parse(id).map { _id =>
      val bsonId = $("_id" -> _id)
      Database.feeds.find(bsonId).one[FeedItem].map { feedOpt =>
        feedOpt.map { feed =>
          Ok($("feed" -> feed))
        } getOrElse Ok($("feed" -> ""))
      } fallbackTo Future.successful(InternalServerError)
    } getOrElse Future.successful(BadRequest)
  }
  def add = Action.async(parse.json) { implicit req =>
    authorized { user =>
      req.body.validate[FeedData].map { feed =>
        val newFeed = feed.itemify(user.username)
        val selector = $("username" -> newFeed.username)
        val update = $("$push" -> $("feeds" -> newFeed._id))
        Database.users.update(selector, update)
        Database.feeds.insert(newFeed).map { err =>

          Created($("id" -> newFeed._id.stringify))
        }

      }.getOrElse(Future.successful(BadRequest("Bad Json")))
    }
  }
  def remove(id : String) = Action.async {
    Database.feeds.remove( BSONDocument("_id" -> BSONObjectID(id))) map { err =>
      Ok
    }
  }

  def yours() = Action.async { implicit req =>
    authorized { user =>
      val feeds = user.feeds.getOrElse(List.empty)
      getFeedsByList(feeds).map { a => Ok($("feeds" -> a))}
    }
  }
  def active() = Action.async { implicit req =>
    authorized { user =>
      val feeds = user.activeFeeds.getOrElse(List.empty)
      getFeedsByList(feeds).map { a => Ok($("feeds" -> a))}
    }
  }

  val getFeedsByList = Memoize {1.hour} { feeds: List[BSONObjectID] =>
    feeds.length match {
      case 0 => Future.successful(List[JsObject]())
      case _ => Database.feeds.find($("_id" -> $("$in" -> $arr(feeds)))).cursor[JsObject].collect[List]()
    }
  }

  def take(id: String) = Action.async (implicit req => authorized { user =>
    async {
      val _id = BSONObjectID(id)
      val selector = $("_id" -> _id)
      val feed = await(Database.feeds.find(selector).one[FeedItem]).get
      val newfeed = feed.copy(attendants = Attendant(user.username) :: feed.attendants)

      //TODO SCHEDULE AUTOREMOVAL

      await(Database.feeds.update(selector, $("$push" -> $("attendants" -> Attendant(user.username)))))
    } map { _ =>
      Ok($("success" -> "true"))
    } fallbackTo Future.successful(BadRequest)
  })

  def acceptTaking(id: String, contractor : String) = Action.async(implicit req => authorized { user =>
    async {
      val _id = BSONObjectID(id)

      val idSelector = $("_id" -> _id)
      val contractorSelector = $("username" -> contractor)

      val feed = await(Database.feeds.find(idSelector).one[FeedItem]).get

      val feedUpdate        = Database.feeds.update(idSelector,         $("$set" -> $("contractor" -> contractor)))
      val contractorUpdate  = Database.users.update(contractorSelector, $("$push" -> $("activeFeeds" -> feed._id)))

      (await(feedUpdate) , await(contractorUpdate))
    } map { _ =>
      Ok
    } fallbackTo Future.successful(BadRequest)
  })

  def nextStage(id: String, stage: Int) = Action.async (implicit  req => authorized { user =>
    playFuture {
      async {
        val feedSelector = $("_id" -> BSONObjectID(id))
        val feed = await(Database.feeds.find(feedSelector).one[FeedItem]).get
        feed.stage match {
          case _ => 0
        }
        await(Database.feeds.update(feedSelector, $("$set" -> $("stage" -> stage))))
      }
    }
  })

  def playFuture(block : => Future[_]) : Future[Result] = {
    block.map {
      case r: JsObject => Ok($("success" -> "true", "result" -> r))
      case _ => Ok($("success" -> "true"))
    } fallbackTo Future.successful(BadRequest)
  }
}