package controllers

import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.json.Json
import play.api.mvc._
import play.twirl.api.Html
import reactivemongo.bson.BSONObjectID
import services.Mail
import services.database.Database
import services.helpers.{Helpers, Memoize}
import play.api.libs.concurrent.Execution.Implicits._

import Helpers._
import scala.concurrent.Future
import scala.concurrent.duration._

// Not unused at all. Do not delete
import play.modules.reactivemongo.json.BSONFormats._

object Newsletter extends Controller {


  implicit val newsletterFormat = Json.format[NewsletterUser]

  case class NewsletterForm(email : String, fullname : String)
  val newsletterForm = Form( mapping(
    "email" -> text,
    "fullname" -> text
  )(NewsletterForm.apply)(NewsletterForm.unapply))

  def register = Action.async { implicit req =>
    val NewsletterForm(email, fullname) = newsletterForm.bindFromRequest().value.get
    val ni = NewsletterUser(BSONObjectID.generate, email, Option(fullname))
    Database.newsletter.insert(ni).map { _ =>
      val mail = views.html.newsletter.welcome(fullname, Contents.hello, Contents.leftContent, Contents.rightContent, email)
      Mail.sendMail(Contents.witaj, mail)(List(email))
      Ok
    } fallbackTo (Future successful BadRequest)
  }
  def unregister(email: String) = Action.async{
    Database.newsletter.remove($("email" -> email)).map { _ =>
      Ok
    } fallbackTo Future.successful(InternalServerError)
  }

  def sendHello(email : String, name : Option[String]) = Action { implicit req =>
    val mail = views.html.newsletter.welcome(name.getOrElse(""), Contents.hello, Contents.leftContent, Contents.rightContent, email)
    Mail.sendMail("Witaj!", mail)(List(email))
    Ok(s"Mail sent to $name")
  }
  val newsForm = Form(mapping(
    "title" -> text,
    "content" -> text
  )(NewsForm.apply)(NewsForm.unapply))
  def sendToAll() = Action.async { implicit req =>
    newsForm.bindFromRequest().value.map { item =>
      Database.newsletter.find($()).cursor[NewsletterUser].collect[List]().map { users =>
        for (user <- users) {
          val mail = views.html.newsletter.welcome(
            user.fullname.getOrElse(""),
            Html(item.content),
            Contents.leftContent,
            Contents.rightContent,
            user.email
          )
          Mail.sendMail(item.title, mail)(List(user.email))
        }
        Ok
      } fallbackTo Future.successful(InternalServerError)
    } getOrElse Future.successful(BadRequest)
  }


  def mailTemplate() = Action { implicit  req =>
    Ok(views.html.newsletter.welcome("", Contents.hello, Contents.leftContent, Contents.rightContent, ""))
  }
}


object Contents {
  val hello = Html(
    """
       Cieszymy się, że zechciałeś dołączyć do newslettera LanceIt. Razem z zespołem dokonujemy
       pełnych starań aby wypuścić platformę jak najwcześniej.<br>
       Jednocześnie informujemy, że pierwsi użytkownicy będą nagradzani wyjątkowymi nagrodami i przywilejami.

       Pozdrawiam,
       Krzysztof Wende,
       LanceIt
    """.stripMargin.trim.replace("\n","<br>"))
  val leftContent = Html("Zlecaj pracę")
  val rightContent = Html("Wykonuj zlecenia")
  val witaj = "Witaj!"

}
case class NewsletterUser(_id : BSONObjectID, email : String, fullname : Option[String])
case class NewsForm(title: String, content : String)