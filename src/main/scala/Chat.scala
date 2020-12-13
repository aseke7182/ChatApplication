import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, scaladsl}

object Chat {

  sealed trait Command

  case class Message(msg: String, userName: String) extends Command

  case class Subscribe(userRef: ActorRef[User.Command]) extends Command

  case class Unsubscribe(userRef: ActorRef[User.Command]) extends Command

  case object ShowSubscribers extends Command

  def apply(chatName: String, subscribers: Seq[ActorRef[User.Command]] = Seq.empty): Behavior[Command] =
    Behaviors.setup[Command] { _ =>
      Behaviors.receiveMessage {
        case Message(msg, userName) =>
          subscribers.foreach(_ ! User.GetMessage(msg, userName))
          Behaviors.same
        case Subscribe(subscriber) =>
          val newSubscribers: Seq[ActorRef[User.Command]] = subscribers :+ subscriber
          apply(chatName, newSubscribers)
        case Unsubscribe(subscriber) =>
          val newSubscribers: Seq[ActorRef[User.Command]] = subscribers.filterNot(_ == subscriber)
          apply(chatName, newSubscribers)
        case ShowSubscribers =>
          println(subscribers.mkString(" "))
          Behaviors.same
      }
    }
}