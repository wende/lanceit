package services.database

import play.Logger
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Collection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.{BSONDocument, BSONInteger}
import services.database.DatabaseTemplate._
import play.api.libs.concurrent.Execution.Implicits._

object Database {

  val persons: JSONCollection = ttl(){ db.collection[JSONCollection]("persons")}
  val feeds : JSONCollection = ttl(){ db.collection[JSONCollection]("feeds")}


  def ttl( field : String = "Date")(collection : => JSONCollection)  =
  {
    collection.indexesManager.ensure(Index(
      Seq((field, IndexType(BSONInteger(1)))), Some(field),
      unique = false,
      background = false,
      dropDups = false,
      sparse = false, None,
      BSONDocument( "expireAfterSeconds" -> 0 )
    )).map( Logger info _.toString)
    collection
  }
}
