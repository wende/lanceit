package services.database

import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Collection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.{BSONDocument, BSONInteger}
import services.database.DatabaseTemplate._
import play.api.libs.concurrent.Execution.Implicits._

object Database {

  def persons: JSONCollection = ttl{ db.collection[JSONCollection]("persons")}
  def feeds : JSONCollection = ttl{ db.collection[JSONCollection]("feeds")}


  def ttl(collection : => JSONCollection)(implicit field : String = "expireAt") =
  {
    collection.indexesManager.ensure(Index(
      key = Seq((field, IndexType(BSONInteger(1)))),
      name = Some(field),
      options = BSONDocument( "expireAfterSeconds" -> 0 )
    ))
    collection
  }
}
