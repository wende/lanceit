package controllers

import actors.WebSocketActor
import controllers.helpers.{Helpers, Memoize}
import controllers.helpers.Memoize.functionToMemoizer
import models.JsonFormats._
import models.User
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Forms._
import play.api.data._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.twirl.api.Html
import services.Mail
import services.database.Database

import scala.concurrent.Future
import scala.concurrent.duration._

object Tests extends Controller {


  def memoization(param: Int) = Action {
    val result2 = Helpers.time (bigComputationM(param))
    val result = Helpers.time (bigComputation(param))
    Ok(Html(s"Times: <br> ${result2._2} vs ${result._2} <br> ${ ((result2._2.toDouble / result._2)*100)}% of original time"))
  }
  val bigComputationM = Memoize(30 seconds)(bigComputation)
  def bigComputation(a: Int) = (0 to a).map( a => Math.sqrt(a)).map(Math.sin).product
}