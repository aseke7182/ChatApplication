import java.util.Calendar

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatGroupActor {

  sealed trait Command

  case class Message(msg: String, userName: String) extends Command

  case class Subscribe(userRef: ActorRef[UserActor.Command]) extends Command

  case class Unsubscribe(userRef: ActorRef[UserActor.Command]) extends Command

  case object ShowSubscribers extends Command

  case class GetUser(msg: MessageClass, replyTo: ActorRef[String]) extends Command

  def apply(chatName: String, subscribers: Seq[ActorRef[UserActor.Command]] = Seq.empty): Behavior[Command] =
    Behaviors.setup[Command] { _ =>
      Behaviors.receiveMessage {
        case Message(msg, userName) =>
          val currentTime = Calendar.getInstance().getTime.toString.split(" ").drop(2).mkString(" ")
          subscribers.foreach(_ ! UserActor.GetMessage(userName, currentTime, msg))
          Behaviors.same
        case Subscribe(subscriber) =>
          val newSubscribers: Seq[ActorRef[UserActor.Command]] = subscribers :+ subscriber
          apply(chatName, newSubscribers)
        case Unsubscribe(subscriber) =>
          val newSubscribers: Seq[ActorRef[UserActor.Command]] = subscribers.filterNot(_ == subscriber)
          apply(chatName, newSubscribers)
        case ShowSubscribers =>
          println(subscribers.mkString(" "))
          Behaviors.same
        case GetUser(msg, replyTo) =>
          val subscribs = subscribers.filter(x => x.path.name == msg.userName)
          if (subscribs.nonEmpty) {
            subscribs.head ! UserActor.PostMessage(msg.msg)
            replyTo ! "message send"
          } else {
            replyTo ! "user not Found"
          }
          Behaviors.same
      }
    }
}