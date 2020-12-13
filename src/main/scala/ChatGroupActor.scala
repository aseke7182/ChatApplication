import java.util.Calendar

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatGroupActor {

  sealed trait Command

  case class Message(msg: String, userName: String) extends Command

  case class Subscribe(userRef: ActorRef[UserActor.Command]) extends Command

  case class Unsubscribe(userRef: ActorRef[UserActor.Command]) extends Command

  case class GetUsers(replyTo: ActorRef[Seq[ActorRef[UserActor.Command]]]) extends Command

  case class GetUser(userName: String, replyTo: ActorRef[Either[ActorRef[UserActor.Command], UserNotFound]]) extends Command

  final case class UserNotFound(userName: String) extends Exception(s"User with username: ${userName} not found.")

  def apply(chatName: String, subscribers: Seq[ActorRef[UserActor.Command]] = Seq.empty): Behavior[Command] =
    Behaviors.setup[Command] { _ =>
      Behaviors.receiveMessage {
        case Message(msg, userName) =>
          val currentTime = Calendar.getInstance().getTime.toString.split(" ").drop(2).mkString(" ")
          subscribers.foreach(_ ! UserActor.GetMessage(userName, currentTime, msg))
          Behaviors.same
        case Subscribe(subscriber) =>
          val newSubscribers: Seq[ActorRef[UserActor.Command]] = subscribers :+ subscriber
          println(newSubscribers)
          apply(chatName, newSubscribers)
        case Unsubscribe(subscriber) =>
          val newSubscribers: Seq[ActorRef[UserActor.Command]] = subscribers.filterNot(_ == subscriber)
          apply(chatName, newSubscribers)
        case GetUsers(replyTo) =>
          replyTo ! subscribers
          Behaviors.same
        case GetUser(userName, replyTo) =>
          val foundSubscribers = subscribers.filter(_.path.name == userName)
          if (foundSubscribers.nonEmpty) {
            replyTo ! Left(foundSubscribers.head)
          } else {
            replyTo ! Right(UserNotFound(userName))
          }

          Behaviors.same
      }
    }
}