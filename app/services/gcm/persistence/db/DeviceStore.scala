package services.gcm.persistence.db

import models.Device
import reactivemongo.core.commands.LastError

import scala.concurrent.Future

trait DeviceStore {
  def store(implicit device: Device) : Future[LastError]
  def all() : Future[List[Device]]
  def findByRegistrationId(regId : String) : Future[Option[Device]]
  def delete(device : Device) : Future[LastError]
  def updateRegistrationId(oldId: String, newId: String): Future[LastError]
}
