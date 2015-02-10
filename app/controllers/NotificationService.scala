package controllers

import infrastructure.NotificationSender
import models.Device
import play.api.mvc.{Result, Action, Controller}
import services.database.Database
import services.gcm.persistence.db.MongoDeviceStorage
import views.html

import scala.concurrent.{Await, Future}
import scala.util.parsing.json.JSONArray
import scala.async.Async._
import models.Notification.NotificationFormat
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json.fromJson
import utils.Results._


object NotificationService extends Controller {

  implicit val storage = new MongoDeviceStorage(Database.gcm)

  def register = Action(parse.json) {
    implicit request =>
      Device createAndStore (request.body \ "registrationId").as[String]
      Ok("Device registered")
  }

  def unregister(regId: String) = Action {
    Device.delete(regId)
    Ok("Device unregistered")
  }

  def pushNotification = Action.async(parse.json) { implicit request =>
      async[Status] {
        val allRegistrationIds = await(Device.allRegistrationIds)
        val promiseOfMulticastResults = Future.sequence(NotificationSender push(fromJson(request.body).get, allRegistrationIds))


        val results = await(promiseOfMulticastResults.toResults)
        results zip Stream.from(0) map {
          case (result, currentDeviceIndex) => NotificationSender handleResult(allRegistrationIds(currentDeviceIndex), result)
        }
        BadRequest
      } recover {case _ => InternalServerError}
  }

  def index = Action {
    Ok(html.index())
  }


  def list = Action.async {
    implicit request =>
      Device.allRegistrationIds.map { a => Ok(JSONArray(a).toString())}
  }

}
