package controllers

import actors.WebSocketActor
import models.JsonFormats._
import models.{Title, Code, User}
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.twirl.api.Html
import services.Mail
import services.database.Database
import services.helpers.Helpers._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object PromoCodes extends Controller {
  def generate(title: String) = Action {
    Code.generate(title) match {
      case Success(code) => Ok(code.code)
      case Failure(_) => BadRequest("Wrong code")
    }
  }
  def verify(code : String) = Action {
    Code.parse(code) match {
      case Success(title) => Ok("Success verifying Your code for title: " + title.name)
      case Failure(_) => BadRequest("Wrong code")
    }
  }
}