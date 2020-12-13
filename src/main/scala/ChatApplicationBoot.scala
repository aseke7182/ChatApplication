import UserActor.{GetChatLog, PostMessage}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.util.Try

object ChatApplicationBoot {
  def main(args: Array[String]): Unit = {
    implicit val log: Logger = LoggerFactory.getLogger(getClass)

    val rootBehavior = Behaviors.setup[Nothing] { context =>

      implicit val system: ActorSystem[_] = context.system
      implicit val executionContext: ExecutionContext = context.executionContext

      val chat = context.spawn(ChatGroupActor("group"), "Chat")
      val user1 = context.spawn(UserActor("user1", chat), "user1")
      val user2 = context.spawn(UserActor("user2", chat), "user2")
      val user3 = context.spawn(UserActor("user3", chat), "user3")

      user1 ! PostMessage("Hello")
      user2 ! PostMessage("Hello mate")
      user3 ! PostMessage("Hello mateeeee")


      val host = "localhost"
      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(9000)

      HttpServer.startHttpServer(new MyRouter(user3).route, host, port)(context.system, context.executionContext)
      Behaviors.empty
    }

    val system = ActorSystem[Nothing](rootBehavior, "ChatApplication")
  }
}
