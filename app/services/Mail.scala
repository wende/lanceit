package services
import com.typesafe.plugin._
import play.api._
import play.api.mvc._
import play.api.Play.current

object Mail {

  def sendMail(subject: String, content: String)(to: List[String]) =
  {
    val mail = use[MailerPlugin].email
    mail.setSubject(subject)
    //or use a list
    mail.setBcc(to:_*)
    mail.setFrom("Lanceit <noreply@lanceit.com>")
    //adds attachment
    // mail.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"))
    // adds inline attachment from byte array
    // val data: Array[Byte] = "data".getBytes
    // mail.addAttachment("data.txt", data, "text/plain", "A simple file", EmailAttachment.INLINE)
    //sends html
    // mail.sendHtml("<html>html</html>" )
    //sends text/text
    // mail.send( "text" )
    //sends both text and html
    val text = content.replaceAll("<.*?>", "")
    mail.send( text, content)
  }

}
