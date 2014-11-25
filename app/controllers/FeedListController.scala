package controllers

import models.FeedItem
import play.api.Logger
import play.api.mvc.BodyParsers._
import play.api.mvc._
import play.api.libs.json._
import models.JsonFormats._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.bson.{BSONObjectID, BSONDocument}

import services.database._

import scala.concurrent.Future

object FeedListController extends Controller{
  def get = Action.async {
    Database.feeds.find( Json.obj() ).cursor[FeedItem].collect[List]().map {
      feeds => Ok(Json.toJson( Json obj "feeds" ->feeds ))
    }
  }
  def add = Action.async(parse.json) { req =>
    req.body.validate[FeedItem].map { feed =>
      val newFeed = feed.copy(Some(BSONObjectID.generate))
      Database.feeds.insert(newFeed).map { err => Created(err.toString + newFeed._id.toString) }
      }.getOrElse(Future.successful(BadRequest("Bad Json")))
    }
  }

