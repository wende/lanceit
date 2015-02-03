package services.database

import play.Logger
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Collection
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.{BSONString, BSONDocument, BSONInteger}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api._


object Database extends Controller with MongoController{

  val users: JSONCollection = unique("username") { db.collection[JSONCollection]("users")}
  val feeds : JSONCollection = index("expireAt") { db.collection[JSONCollection]("feeds")}

  def index(field : String)(collection : => JSONCollection) = {
    collection.indexesManager.ensure(Index(
      Seq((field, IndexType(BSONInteger(1)))), Some(field),
      unique = false,
      background = false,
      dropDups = false,
      sparse = false, None,
      BSONDocument()
    )).map( Logger info _.toString)
    collection
  }
  def ttl( field : String = "expireAt")(collection : => JSONCollection)  = {
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
  def unique(field: String)(collection : => JSONCollection) = {
    collection.indexesManager.ensure(Index(
      Seq((field, IndexType(BSONInteger(1)))), Some(field),
      unique = true
    ))
    collection
  }
}
