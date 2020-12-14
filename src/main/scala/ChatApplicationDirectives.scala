import ACLActor.{UserNotFound, UserNotSubscribed}
import akka.http.scaladsl.server.{Directive1, Directives}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ChatApplicationDirectives extends Directives {

  def handleWithEither[T, Y](f: Future[Either[T, Y]]): Directive1[T] =
    onComplete(f) flatMap {
      case Success(t) =>
        t match {
          case Left(userRef) =>
            provide(userRef)
          case Right(error) =>
            val apiError = handleError(error)
            complete(apiError.statusCode, apiError.message)
        }

      case Failure(error) =>
        val apiError = handleError(error)
        complete(apiError.statusCode, apiError.message)
    }

  def handleWithGeneric[T](f: Future[Seq[T]]): Directive1[Seq[T]] = onComplete(f) flatMap {
    case Success(t) =>
      provide(t)
    case Failure(error) =>
      val apiError = handleError(error)
      complete(apiError.statusCode, apiError.message)
  }

  private def handleError[Y](e: Y): ApiError = e match {
    case UserNotFound(userName) => ApiError.userNotFound(userName)
    case UserNotSubscribed(userName) => ApiError.userNotSubscribed(userName)
    case _ => ApiError.generic
  }
}