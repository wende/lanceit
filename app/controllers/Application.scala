package controllers

import actors.WebSocketActor
import models.User
import play.api._
import play.api.cache.Cache
import play.api.libs.json.JsObject
import play.modules.reactivemongo.MongoController
import services.Mail
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import services.database.Database
import views.html.{index, head}
import scala.concurrent.duration._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import models.JsonFormats._
import scala.pickling._
import scala.pickling.json._

object Application extends Controller {

  val USER_CACHE = "username"

  def index = Action {
    Redirect("assets/homepage/index-android.html")
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
      Database.users.find(query).one[User].map[Result]{ r => r.map { user =>
        val u = user.copy(password = "****")
        Ok(Json.toJson(u)).withSession(USER_CACHE -> u.username)
      } getOrElse BadRequest("Wrong login or password")
    }.fallbackTo {
        Future.successful(InternalServerError)
      }
    } getOrElse Future.successful(BadRequest("Wrong parameters"))
  }
  def register = Action.async { req =>
    val selector = Json.obj("unique" -> true)
    Json.fromJson[User](req.body.asJson.get).map { user =>
      Database.users.insert(user).map { err =>
        Created(Json.obj("success" -> "true", "err" -> err.updatedExisting))
      } fallbackTo Future.successful(BadRequest(Json.obj("err" -> "User already exists")))
    } getOrElse Future.successful(BadRequest(Json.obj("err" -> "Insufficient parameters")))
  }
  import Predef.implicitly
  def authorized(block : (User) => Future[Result] )(implicit req : Request[_]) : Future[Result] = {
    req.session.get(USER_CACHE).map { session =>
      val user = Cache.getAs[User](USER_CACHE)
      if (user.isEmpty) {
        Database.users.find(Json.obj(USER_CACHE -> session)).one[User].flatMap[Result] { r => r.map { res =>
          val u = res.copy(password = "****")
          Cache.set(USER_CACHE,u, 1.hour)
          block(u)
          }.get
        } fallbackTo Future.successful(InternalServerError)
      } else {
        Cache.set(USER_CACHE, user, 1.hour)
        block(user.get)
      }
    }
  } getOrElse Future.successful(Unauthorized)

  def socket = WebSocket.acceptWithActor[JsValue, JsValue](req => out => WebSocketActor.props(out))

  def mail = Action {
    Mail.sendMail("Subject", "<b>content<b> asdasd </br>")(List("iraasta@gmail.com"))
    Ok("")
  }
}