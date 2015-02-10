package models

import persistence.db.H2DbDeviceStorage
import services.gcm.persistence.db.{DeviceStore, MongoDeviceStorage}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Class to represent a device, we only store registration id but we could link the device to an user account
 * in a real application.
 */
case class Device(var registrationId: String)

object Device {

  def createAndStore(regId: String)(implicit storage: DeviceStore) { storage store Device(regId) }

  def delete(regId: String)(implicit storage: DeviceStore) { storage delete Device(regId) }

  def allRegistrationIds(implicit storage: DeviceStore, ec: ExecutionContext): Future[List[String]] = { storage.all map (x => x.map {_.registrationId}) }
}
