import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

trait Router {
  def route: Route
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

  def chatApplicationRoute: Route = {
    pathPrefix("chat-application") {
      pathEndOrSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "you are in chat app"))
        }
      }
    }
  }

  override def route: Route = {
    concat(
      healthCheckRoute,
      chatApplicationRoute
    )
  }

}