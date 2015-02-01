package controllers.helpers

import play.api.libs.json.{JsArray, Json, JsObject}
import play.api.libs.json.Json.JsValueWrapper
import reactivemongo.bson.BSONObjectID

import scala.pickling._

object WendeHelpers {
  implicit def tupleToJson(a : (String, JsValueWrapper )) : JsObject  = Json.obj(a)
  implicit def arrToJson(a : List[JsValueWrapper] ) : JsArray  = Json.arr(a:_*)

}
object Vinegar {
  implicit def BSONObjectIdPickler(implicit stringPickler: SPickler[String], stringUnpickler: Unpickler[String], pf: PickleFormat) =
    new SPickler[BSONObjectID] with Unpickler[BSONObjectID] {
      val format: PickleFormat = pf
      def pickle(id: BSONObjectID, builder: PBuilder):Unit = {
        builder.beginEntry(id)
        builder.hintStaticallyElidedType()
        builder.hintTag(FastTypeTag.String)
        builder.pinHints()
        builder.putField("id", { b=>
          stringPickler.pickle(id.stringify,builder)
        })
        builder.unpinHints()
        builder.endEntry()
      }
      def unpickle(tpe: => FastTypeTag[_], preader: PReader): Any = {
        val reader = preader
        reader.hintStaticallyElidedType()
        reader.hintTag(FastTypeTag.String)
        reader.pinHints()
        val r1 = reader.readField("id")
        r1.beginEntryNoTag()
        val id = stringUnpickler.unpickle(FastTypeTag.String, r1).asInstanceOf[String]
        r1.endEntry()
        reader.unpinHints()
        BSONObjectID(id)
      }
    }

}