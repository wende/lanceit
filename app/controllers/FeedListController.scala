package controllers

import models.{FeedData, FeedItem}
import play.api.Logger
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.BodyParsers._
import play.api.mvc._
import play.api.libs.json._
import models.JsonFormats._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import play.modules.reactivemongo.json.BSONFormats._

import services.database._

import scala.concurrent.Future

object FeedListController extends Controller{
  def get = Action.async {
    Database.feeds.find( Json.obj() ).cursor[FeedItem].collect[List]().map {
      feeds => Ok(Json.toJson( Json obj "feeds" -> feeds ))
    }
  }
  def getById(id: String) = Action.async {
    val bsonId = BSONObjectID(id)
    Future.successful(Ok)
  }
  import Application.authorized
  def add = Action.async(parse.json) { implicit req =>
    authorized { user =>
      req.body.validate[FeedData].map { feed =>
        val newFeed = feed.itemify(user.username)
        Database.feeds.insert(newFeed).map { err => Created(newFeed._id.toString())}
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
      val feeds = user.activeFeeds.getOrElse(List.empty)
      if(feeds.length != 0) {
        Database.feeds.find(Json.obj("_id" -> Json.obj("$in" -> Json.arr(feeds)))).cursor[JsObject].collect[List]()
          .map { a => Ok(a.toString)}
      } else {
        Future.successful(Ok(Json.obj()))
      }
    }
  }

  def active() = play.mvc.Results.TODO
  //def yours() = play.mvc.Results.TODO
}


