import actors.ChatGroupActor.Subscribe
import actors.UserActor.PostMessage
import actors.{ACLActor, ChatGroupActor, UserActor}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import http.HttpServer
import org.slf4j.{Logger, LoggerFactory}
import router.MyRouter

import scala.concurrent.ExecutionContext
import scala.util.Try

object ChatApplicationBoot {
  def main(args: Array[String]): Unit = {
    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[_] = context.system
      implicit val executionContext: ExecutionContext = context.executionContext

      val chat = context.spawn(ChatGroupActor("group"), "Chat")
      val accessControl = context.spawn(ACLActor(chat), "ACLActor")
      val user1 = context.spawn(UserActor("user1", chat, accessControl), "user1")
      val user2 = context.spawn(actors.UserActor("user2", chat, accessControl), "user2")
      val user3 = context.spawn(actors.UserActor("user3", chat, accessControl), "user3")


      val host = "0.0.0.0"
      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(9000)

      chat ! Subscribe(user1)
      chat ! Subscribe(user2)
      chat ! Subscribe(user3)
      user1 ! PostMessage("Hello")
      user2 ! PostMessage("Hello mate")
      user3 ! PostMessage("Hello mateeeee")

      HttpServer.startHttpServer(new MyRouter(chat, accessControl).route, host, port)(context.system, context.executionContext)
      Behaviors.empty
    }

    val system = ActorSystem[Nothing](rootBehavior, "ChatApplication")
  }
}
