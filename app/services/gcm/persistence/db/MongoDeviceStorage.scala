package services.gcm.persistence.db

import models.Device
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class MongoDeviceStorage(collection : JSONCollection) extends DeviceStore {
  override def store(device: Device): Future[LastError] = {
    val obj = Json.obj("id" -> device.registrationId)
    collection.insert(obj)
  }

  override def findByRegistrationId(regId: String): Future[Option[Device]] = {
    val obj = Json.obj("id" -> regId)
    collection.find(obj).one[JsObject].map { _.map {_ \ "id" toString} map Device.apply}
  }

  override def all(): Future[Array[Device]] = ???

  override def updateRegistrationId(oldId: String, newId: String): Future[LastError] = ???

  override def delete(device: Device): Future[LastError] = ???
}
