package controllers

import infrastructure.NotificationSender
import models.Device
import play.api.mvc.{Action, Controller}
import views.html

import scala.concurrent.Future
import scala.util.parsing.json.JSONArray


object NotificationService extends Controller {

  def register = Action(parse.json) {
    implicit request =>
      Device createAndStore (request.body \ "registrationId").as[String]
      Ok("Device registered")
  }

  def unregister(regId: String) = Action {
    Device.delete(regId)
    Ok("Device unregistered")
  }

  def pushNotification() = Action.async(parse.json) {
    implicit request =>
      import models.Notification.NotificationFormat
      import play.api.libs.concurrent.Execution.Implicits._
      import play.api.libs.json.Json.fromJson
      import utils.Results._

      val allRegistrationIds = Device.allRegistrationIds
      val promiseOfMulticastResults = Future.sequence(NotificationSender push(fromJson(request.body).get, allRegistrationIds))


      promiseOfMulticastResults.toResults.map(results => {
        results zip Stream.from(0) map {
          case (result, currentDeviceIndex) => NotificationSender handleResult(allRegistrationIds(currentDeviceIndex), result)
        }
        NoContent
      }).recover({
        case _ => NoContent
      })

  }

  def index = Action {
    Ok(html.index())
  }


  def list = Action {
    implicit request =>
      Ok(JSONArray(Device.allRegistrationIds.toList).toString())
  }

}
