import java.util.Calendar

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
    val item = FeedItem(BSONObjectID.generate, "iraasta","Title","Desc",time ,0,0,BSONDateTime(time + 1000*60*60*24*7))
    Logger.info(Json.toJson(item).toString())
    Database.feeds.insert(item)
  }
}