import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object UserActor {

  sealed trait Command

  case class PostMessage(msg: String) extends Command

  case class GetMessage(from: String, time: String, msg: String) extends Command

  case class GetChatLog(replyTo: ActorRef[List[String]]) extends Command

  def apply(userName: String, chatRef: ActorRef[ChatGroupActor.Command], chatLog: List[String] = List.empty): Behavior[Command] =
    Behaviors.setup[Command] { context =>

      if (chatRef != null) {
        chatRef ! ChatGroupActor.Subscribe(context.self)
      }

      Behaviors.receiveMessage {
        case PostMessage(msg) =>
          chatRef ! ChatGroupActor.Message(msg, userName)
          Behaviors.same
        case GetMessage(from, time, msg) =>
          val messageLog: String = s"${time} | ${from}: ${msg}"
          //          println(messageLog) // DEBUG
          apply(userName, chatRef, messageLog +: chatLog)
        case GetChatLog(replyTo) =>
          replyTo ! chatLog
          Behaviors.same
      }
    }
}