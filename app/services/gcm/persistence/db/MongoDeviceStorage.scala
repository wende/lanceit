package services.gcm.persistence.db

import models.Device
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands.LastError
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class MongoDeviceStorage(collection : JSONCollection) extends DeviceStore {
  def query(implicit device : Device) = Json.obj("id" -> device.registrationId)
  def query(id : String) = Json.obj("id" -> id)

  override def store(implicit device: Device): Future[LastError] = {
    collection.insert(query)
  }

  override def findByRegistrationId(regId: String): Future[Option[Device]] = {
    collection.find(query(regId)).one[JsObject].map { _.map {_ \ "id" toString} map Device.apply}
  }

  override def all(): Future[List[Device]] = {
    collection.find(Json.obj()).cursor[JsObject].collect[List]().map{ _.map {_ \ "id" toString} map Device.apply}
  }

  override def updateRegistrationId(oldId: String, newId: String): Future[LastError] = {
    collection.update(query(oldId), query(newId))
  }

  override def delete(device: Device): Future[LastError] = {
    collection.remove(query(device))
  }
  override def delete(id: String) = delete(Device(id))
}
