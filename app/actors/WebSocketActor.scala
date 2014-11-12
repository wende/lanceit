package actors

import akka.actor.Actor.Receive
import akka.actor._
import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._

class WebSocketActor(out: ActorRef) extends Actor{
  implicit val rds = (
      (__ \ 'event).read[Int] and
      (__ \ 'data).read[String]
    ) tupled
  override def receive: Receive =
  {

    case json:JsObject =>

      json.validate[(Int,String)].map { case (event, data) =>
          Logger.info(s"Event $event message $data")
        }
    case o => Logger.info( o.getClass.getSimpleName)

  }
}
object WebSocketActor
{
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

