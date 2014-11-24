import java.util.Calendar

import models.FeedItem
import play._
import play.api.libs.json._
import reactivemongo.bson.BSONDateTime
import services.database.Database
import models.JsonFormats._
import play.api.libs.concurrent.Execution.Implicits._


class Global extends GlobalSettings {

  override def onStart(application :Application)
  {
    Logger.info("Server started and configured")
    val item = FeedItem("iraasta","Title","Desc", Calendar.getInstance().getTimeInMillis,0,0,BSONDateTime(0))
    Logger.info(Json.toJson(item).toString())
    Database.feeds.insert(item)
  }
}