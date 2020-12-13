import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object User {

  sealed trait Command

  case class PostMessage(msg: String) extends Command

  case class GetMessage(msg: String, from: String) extends Command

  def apply(userName: String, chatRef: ActorRef[Chat.Command]): Behavior[Command] =
    Behaviors.setup[Command] { context =>

      if (chatRef != null) {
        chatRef ! Chat.Subscribe(context.self)
      }

      Behaviors.receiveMessage {
        case PostMessage(msg) =>
          chatRef ! Chat.Message(msg, userName)
          Behaviors.same
        case GetMessage(msg, from) =>
          println(s"The user ${userName} received from ${from} the following message: ${msg}")
          Behaviors.same
      }
    }
}