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

      val host = "localhost"
      val port = Try(System.getenv("PORT")).map(_.toInt).getOrElse(9000)

      HttpServer.startHttpServer(new MyRouter().route, host, port)(context.system, context.executionContext)
      Behaviors.empty
    }

    val system = ActorSystem[Nothing](rootBehavior, "ChatApplication")
  }
}
