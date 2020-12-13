import ChatGroupActor.GetUser
import UserActor.Command
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
  def route: Route
}

class MyRouter(user: ActorRef[Command],chat: ActorRef[ChatGroupActor.Command])(implicit system: ActorSystem[_], ex: ExecutionContext)
  extends Router
    with Directives {

  implicit lazy val timeout: Timeout = Timeout(5.seconds)
  implicit lazy val scheduler: Scheduler = system.scheduler

  def healthCheckRoute: Route = {
    path("ping") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "pong"))
      }
    }
  }

  def chatApplicationRoute: Route = {
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
              val result = chat.ask[String](ref => GetUser(msg, ref))
              complete(result)
            }
          }
        },
        path("getChatLog") {
          pathEndOrSingleSlash {
            get {
              val processFuture = user.ask[List[String]](ref => UserActor.GetChatLog(ref))
              complete(processFuture)
            }
          }
        }
      )
    }
  }

  override def route: Route = {
    concat(
      healthCheckRoute,
      chatApplicationRoute
    )
  }

}