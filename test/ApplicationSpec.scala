import models.User
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import reactivemongo.bson.BSONObjectID
import scala.pickling._

// This imports names only
import scala.pickling.json._    // Imports PickleFormat
import scala.pickling.static._  // Avoid runtime pickler
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
case class MyPickle(a : Option[List[BSONObjectID]])

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    /*"send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("HELLO")
    }*/

    "pickle things" in new WithApplication() {
      import controllers.helpers.Vinegar._
      val my = MyPickle(Some(List(BSONObjectID.generate)))
      my must equalTo(JSONPickle(my.pickle.value).unpickle[MyPickle])

      //val my2 = User("a",1,1,"b","c","d","f")
      //my2 must equalTo(JSONPickle(my2.pickle.value).unpickle[MyPickle])
    }

  }
}
