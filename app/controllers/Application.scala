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

import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import models.JsonFormats._


object Application extends Controller {

  def index = Action {
    Ok(views.html.index.render("HELLO"))
  }
  def change = Action {
   Ok
  }

  case class LoginData(username : String, password: String)
  val loginForm = Form(mapping (
    "username" -> text,
    "password" -> text
  )(LoginData.apply)(LoginData.unapply))

  def login = Action.async { implicit req =>
    val loginData = loginForm.bindFromRequest()
    loginData.value.map { form =>
      val query = Json.obj(
        "username" -> form.username,
        "password" -> form.password
      )
      Database.users.find(query).one[User].map[Result]{ r => r.map (user =>
        Ok(Json.toJson(user.copy(password = "****")))
      ) getOrElse(BadRequest)
    }.fallbackTo {
        Future.successful(BadRequest)
      }
    } getOrElse Future.successful(BadRequest)
  }
  def register = Action.async { req =>
    val selector = Json.obj("unique" -> true)
    Json.fromJson[User](req.body.asJson.get).map { user =>
      Database.users.insert(user).map { err =>
        Created(Json.obj("success" -> "true", "err" -> err.updatedExisting))
      } fallbackTo Future.successful(BadRequest(Json.obj("err" -> "User already exists")))
    } getOrElse Future.successful(BadRequest(Json.obj("err" -> "Insufficient parameters")))
  }
  def socket = WebSocket.acceptWithActor[JsValue, JsValue](req => out => WebSocketActor.props(out))

  def mail = Action {
    Mail.sendMail("Subject", "<b>content<b> asdasd </br>")(List("iraasta@gmail.com"))
    Ok("")
  }
}