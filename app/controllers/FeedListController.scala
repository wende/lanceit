package controllers

import controllers.helpers.{Helpers, Memoize}
import models.{FeedData, FeedItem}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.BodyParsers._
import play.api.mvc._
import play.api.libs.json._
import models.JsonFormats._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._

import play.api.Play.current

import services.database._

import scala.concurrent.Future
import scala.concurrent.duration._

object FeedListController extends Controller{
  def get = Action.async {
    val selector = Json.obj("expireAt" -> Json.obj("$gt" -> BSONDateTime(Helpers.now)))
    Database.feeds.find(selector).cursor[FeedItem].collect[List]().map {
      feeds => Ok(Json.toJson( Json obj "feeds" -> feeds ))
    }
  }
  def getById(id: String) = Action.async {
    BSONObjectID.parse(id).map { _id =>
      val bsonId = Json.obj("_id" -> _id)
      Database.feeds.find(bsonId).one[FeedItem].map { feedOpt =>
        feedOpt.map { feed =>
          Ok(Json.obj("feed" -> feed))
        } getOrElse Ok(Json.obj("feed" -> ""))
      } fallbackTo Future.successful(InternalServerError)
    } getOrElse Future.successful(BadRequest)
  }
  import Application.authorized
  def add = Action.async(parse.json) { implicit req =>
    authorized { user =>
      req.body.validate[FeedData].map { feed =>
        val newFeed = feed.itemify(user.username)
        val selector = Json.obj("username" -> newFeed.username)
        val update = Json.obj("$push" -> newFeed._id)
        Database.users.update(selector, update)
        Database.feeds.insert(newFeed).map { err =>

          Created(Json.obj("id" -> newFeed._id.stringify))
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
      getFeedsByList(feeds).map { a => Ok(Json.obj("feeds" -> a))}
    }
  }
  def active() = Action.async { implicit req =>
    authorized { user =>
      val feeds = user.activeFeeds.getOrElse(List.empty)
      getFeedsByList(feeds).map { a => Ok(Json.obj("feeds" -> a))}
    }
  }

  val getFeedsByList = Memoize {1.hour} { feeds: List[BSONObjectID] =>
    feeds.length match {
      case 0 => Future.successful(List[JsObject]())
      case _ => Database.feeds.find(Json.obj("_id" -> Json.obj("$in" -> Json.arr(feeds)))).cursor[JsObject].collect[List]()
    }
  }

  def take(id: String) = Action.async { implicit req =>
    authorized { user =>
      BSONObjectID.parse(id).map { _id =>
        Database.feeds.find(Json.obj("_id" -> _id)).one[FeedItem].map { feedOpt =>
          feedOpt.map { feed =>
            Ok(Json.obj("feed" -> feed))
          } getOrElse Ok(Json.obj("feed" -> ""))
        } fallbackTo Future.successful(InternalServerError)
      } getOrElse Future.successful(BadRequest)
    }
  }

}



