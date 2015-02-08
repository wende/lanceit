import java.util.Calendar

import controllers.Newsletter
import models.FeedItem
import play._
import play.api.libs.json._
import reactivemongo.bson.{BSONObjectID, BSONDateTime}
import services.database.Database
import models.JsonFormats._
import play.api.libs.concurrent.Execution.Implicits._


class Global extends GlobalSettings {

  override def onStart(application :Application)
  {
    val time = Calendar.getInstance().getTimeInMillis
    Logger.info("Server started and configured")
  }
}