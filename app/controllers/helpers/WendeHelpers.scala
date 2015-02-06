package controllers.helpers

import java.util.Calendar

import play.api.Application
import play.api.cache.Cache
import play.api.libs.json.{JsArray, Json, JsObject}
import play.api.libs.json.Json.JsValueWrapper
import play.libs.F.Tuple
import reactivemongo.bson.BSONObjectID

import scala.concurrent.duration._
import scala.pickling._
import scala.reflect.ClassTag

object Helpers {
  def time[R](block: => R): (R, Long) = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    (result, t1 - t0)
  }
  def $(a:(String,JsValueWrapper)*) = Json.obj(a:_*)
  def $arr(a:(JsValueWrapper)*) = Json.arr(a:_*)

  def now = Calendar.getInstance().getTimeInMillis
}
case class Memoize[-T , +R : ClassTag](duration : Duration, refreshOnGet : Boolean = false)(f: T => R)
                                      (implicit app : Application) extends (T => R) {
  val safeCounter = Cache.getAs[Int](Memoize.CACHE_KEY).getOrElse(0)
  Cache.set(Memoize.CACHE_KEY, safeCounter + 1)

  def apply(x: T) : R = {
    val key = safeCounter + x.toString
    val result = Cache.getAs[R](key).getOrElse {
      val result = f(x)
      Cache.set(key, result, duration)
      result
    }
    if(refreshOnGet) Cache.set(key, result, duration)
    result
  }
}
object Memoize {
  implicit def functionToMemoizer[T,R : ClassTag] (f:(T => R)) : Memoizable[T,R] = Memoizable(f)
  lazy val CACHE_KEY = "memoize-safe-counter"
}
case class Memoizable[T,R : ClassTag](f: (T => R) ){
  def memoize(implicit app : Application) = Memoize(1.minute, refreshOnGet = false)(f)
  def memoize(duration : Duration, refreshOnGet : Boolean)(implicit app : Application) = Memoize(duration,refreshOnGet)(f)
}
