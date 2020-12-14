package router

import actors.ACLActor.GetUser
import actors.ChatGroupActor.GetSubscribers
import actors.{ACLActor, ChatGroupActor, UserActor}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import directives.{ChatApplicationDirectives, ValidatorDirectives}
import io.circe.generic.auto._
import models.MessageClass
import validators.{MessageClassValidator, StringValidator}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

trait Router {
  def route: Route
}

class MyRouter(chat: ActorRef[ChatGroupActor.Command], accessControl: ActorRef[ACLActor.Command])(implicit system: ActorSystem[_], ex: ExecutionContext)
  extends Router
    with Directives
    with ChatApplicationDirectives
    with ValidatorDirectives {

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
            entity(as[MessageClass]) { message =>
              validateWith(MessageClassValidator)(message) {
                handleWithEither(accessControl.ask[Either[ActorRef[UserActor.Command], Exception]](ref => GetUser(message.userName, ref))) { userRef =>
                  userRef ! UserActor.PostMessage(message.msg)
                  complete("message sent")
                }
              }
            }
          }
        },
        path("getChatLog") {
          pathEndOrSingleSlash {
            get {
              entity(as[String]) { userName =>
                validateWith(StringValidator)(userName) {
                  handleWithEither(accessControl.ask[Either[ActorRef[UserActor.Command], Exception]](ref => GetUser(userName, ref))) { userRef =>
                    val processFuture = userRef.ask[List[String]](ref => UserActor.GetChatLog(ref))
                    complete(processFuture)
                  }
                }
              }
            }
          }
        },
        path("getUsers") {
          pathEndOrSingleSlash {
            get {
              handleWithGeneric(chat.ask[Seq[ActorRef[UserActor.Command]]](ref => GetSubscribers(ref))) { users =>
                complete(users.map(_.path.name).mkString(" "))
              }
            }
          }
        },
        path("createUser") {
          post {
            entity(as[String]) { userName =>
              validateWith(StringValidator)(userName) {
                complete(s"user with $userName created")
              }
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