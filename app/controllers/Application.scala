package controllers

import actors.WebSocketActor
import models.User
import play.api._
import play.api.libs.json.JsObject
import play.modules.reactivemongo.MongoController
import services.Mail
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import services.database.Database
import views.html.{index, main}

import scala.concurrent.Future


object Application extends Controller {

  def index = Action {
    Ok(views.html.index.render("HELLO"))
  }
  def change = Action {
   Ok
  }
  def login(username: String, password: String) = Action.async {
    val query = Json.obj(
      "username" -> username,
      "password" -> password
    )
    Database.users.find(query).one[User].map[Result]( user =>
      Ok(Json.toJson(user))
    ).fallbackTo {
      Future.successful(BadRequest)
    }
  }
  def socket = WebSocket.acceptWithActor[JsValue, JsValue](req => out => WebSocketActor.props(out))

  def mail = Action {
    Mail.sendMail("Subject", "<b>content<b> asdasd </br>")(List("iraasta@gmail.com"))
    Ok("")
  }
}