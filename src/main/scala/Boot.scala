import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import io.circe.syntax._
import models.MessageClass

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Boot extends App {
  import io.circe.generic.auto._

  implicit val system = ActorSystem("System")
  implicit val ec = system.dispatcher

  for (i <- 1 to 150) {
    post(i.toString)
  }

  def post(msg: String): Unit = {
    val responseFuture: Future[HttpResponse] =
      Http(system).singleRequest(
        HttpRequest(
          HttpMethods.POST,
          uri = "https://bhle-chat-app.herokuapp.com/chat-application/sendMessage",
          entity = HttpEntity(ContentTypes.`application/json`, MessageClass("user1", msg).asJson.toString())
        )
      )

    responseFuture.onComplete({
      case Success(value) =>
        println(value)
      case Failure(exception) =>
        println(exception)
    })
  }
}