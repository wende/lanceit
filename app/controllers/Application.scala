package controllers

import actors.WebSocketActor
import play.api._
import play.api.libs.json.JsObject
import play.api.mvc._
import play.api.Play.current
import play.modules.reactivemongo.MongoController
import services.Mail
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current



object Application extends Controller {


  def index = Action {
    Ok
  }
  def change = Action {
   Ok
  }
  def socket = WebSocket.acceptWithActor[JsValue, JsValue](req => out => WebSocketActor.props(out))

  def mail = Action {
    Mail.sendMail("Subject", "<b>content<b> asdasd </br>")(List("iraasta@gmail.com"))
    Ok("")
  }

}