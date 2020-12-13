import Chat.GetUser
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

trait Router {
  def route(chat: ActorRef[Chat.Command]): Route
}

class MyRouter(implicit system: ActorSystem[_], ex: ExecutionContext)
  extends Router
    with Directives {


  def healthCheckRoute: Route = {
    path("ping") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "pong"))
      }
    }
  }

  def chatApplicationRoute(chat: ActorRef[Chat.Command]): Route = {
    implicit val timeout: Timeout = 3.seconds
    implicit lazy val scheduler: Scheduler = system.scheduler
    pathPrefix("chat-application") {
      concat(
        pathEndOrSingleSlash {
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "you are in chat app"))
          }
        },
        path("sendMessage") {
          post {
            entity(as[MessageClass]) { msg =>
              val result = chat.ask[String](ref=> GetUser(msg,ref))
              complete(result)
            }
          }
        }
      )
    }
  }

  override def route(chat: ActorRef[Chat.Command]): Route = {
    concat(
      healthCheckRoute,
      chatApplicationRoute(chat)
    )
  }

}