package infrastructure

import controllers.NotificationService
import models.Notification
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.android.gcm.server.{Sender, Constants, Result, MulticastResult}
import utils.Chunks._
import scala.collection.JavaConversions._
import play.api.Play.current
import concurrent.Future


object NotificationSender {

  val storage = NotificationService.storage
  val MaxMulticastSize = 1000

  val Sender: Sender = new Sender("AIzaSyAMIo-Xfy-myI91jm4mVyGokoS2Bvty_e8")

  def push(notification: Notification, regIdsList: List[String]): List[Future[MulticastResult]] = {
    val message = notification.asMessage
    (regIdsList.toArray chunk MaxMulticastSize map (regIds => {
      Future (Sender send(message, regIds.toList, 5))
    })).toList
  }

  def handleResult(regId: String, result: Result) {
    Option(result.getMessageId) match {
      case Some(s) => handleMultipleRegistration(regId, Option(result.getCanonicalRegistrationId))
      case None => handleError(regId, result.getErrorCodeName)
    }
  }

  def handleMultipleRegistration(deviceRegistrationId: String, canonicalRegistrationId: Option[String]) {
    if (canonicalRegistrationId.isDefined) {
      storage updateRegistrationId(deviceRegistrationId, canonicalRegistrationId.get)
    }
  }

  def handleError(deviceRegistrationId: String, errorCode: String) {
    errorCode match {
      case Constants.ERROR_NOT_REGISTERED => storage delete (deviceRegistrationId)
      case Constants.ERROR_INVALID_REGISTRATION => storage delete (deviceRegistrationId)
      case _ => // Handle errors as you want
    }
  }
}
