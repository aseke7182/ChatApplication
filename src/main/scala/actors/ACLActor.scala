package actors

import actors.ChatGroupActor.GetSubscribers
import actors.ChatGroupActor.Subscribe
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ACLActor {

  sealed trait Command

  case class GetUser(userName: String, replyTo: ActorRef[Either[ActorRef[UserActor.Command], Exception]]) extends Command

  case class RegisterUser(userRef: ActorRef[UserActor.Command]) extends Command

  case class CreateUser(userName: String, replyTo: ActorRef[Either[ActorRef[UserActor.Command], Exception]]) extends Command

  case class SubscribeUser(userName: String, replyTo: ActorRef[Either[ActorRef[UserActor.Command], Exception]]) extends Command

  final case class UserNotFound(userName: String) extends Exception(s"User with username: ${userName} not found.")

  final case class UserNotSubscribed(userName: String) extends Exception(s"User with username: ${userName} is not subscribed.")

  final case class UserAlreadyExists(userName: String) extends Exception(s"User with username: ${userName} already exists.")

  final case class UserAlreadySubscribed(userName: String) extends Exception(s"User with username: ${userName} already subscribed to the group chat.")

  def apply(chat: ActorRef[ChatGroupActor.Command], registeredUsers: Seq[ActorRef[UserActor.Command]] = Seq.empty): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      context.log.info("ACL actor is starting...")

      working(chat, registeredUsers)
    }

  private def working(chat: ActorRef[ChatGroupActor.Command], registeredUsers: Seq[ActorRef[UserActor.Command]] = Seq.empty): Behavior[Command] =
    Behaviors.setup { context =>
      implicit lazy val timeout: Timeout = Timeout(5.seconds)
      implicit lazy val scheduler: Scheduler = context.system.scheduler
      implicit val ec: ExecutionContext = context.executionContext


      Behaviors.receiveMessage {
        case GetUser(userName, replyTo) =>
          val foundUser = findUser(registeredUsers, userName)

          if (foundUser.nonEmpty) {
            val processFuture = chat.ask[Seq[ActorRef[UserActor.Command]]](ref => GetSubscribers(ref))
            processFuture.onComplete {
              case Success(seqOfSubscribers) =>
                val foundSubscriber = findUser(seqOfSubscribers, userName)

                if (foundSubscriber.nonEmpty) replyTo ! Left(foundSubscriber.head)
                else replyTo ! Right(UserNotSubscribed(userName))
              case Failure(_) =>
                replyTo ! Right(new Exception("Internal error"))
            }
          }
          else {
            replyTo ! Right(UserNotFound(userName))
          }

          Behaviors.same
        case RegisterUser(user) =>
          working(chat, registeredUsers :+ user)
        case CreateUser(userName, replyTo) =>
          val foundUser = findUser(registeredUsers, userName)

          if (foundUser.nonEmpty) {
            replyTo ! Right(UserAlreadyExists(userName))
          } else {
            val newUser = context.spawn(UserActor(userName, chat, context.self), userName)
            context.self ! RegisterUser(newUser)
            replyTo ! Left(newUser)
          }
          Behaviors.same
        case SubscribeUser(userName, replyTo) =>
          val foundUser = findUser(registeredUsers, userName)

          if (foundUser.nonEmpty) {
            val processFuture = chat.ask[Seq[ActorRef[UserActor.Command]]](ref => GetSubscribers(ref))
            processFuture.onComplete {
              case Success(seqOfSubscribers) =>
                val foundSubscriber = findUser(seqOfSubscribers, userName)

                if (foundSubscriber.nonEmpty) replyTo ! Right(UserAlreadySubscribed(userName))
                else {
                  chat ! Subscribe(foundUser.head)
                  replyTo ! Left(foundUser.head)
                }
              case Failure(_) =>
                replyTo ! Right(new Exception("Internal error"))
            }
          }
          else {
            replyTo ! Right(UserNotFound(userName))
          }
          Behaviors.same
      }
    }


  private def findUser[T](someSeq: Seq[ActorRef[T]], userName: String): Seq[ActorRef[T]] =
    someSeq.filter(_.path.name == userName)

}
