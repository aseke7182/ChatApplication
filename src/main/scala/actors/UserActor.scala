package actors

import actors.ACLActor.RegisterUser
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object UserActor {

  sealed trait Command

  case class PostMessage(msg: String) extends Command

  case class GetMessage(from: String, time: String, msg: String) extends Command

  case class GetChatLog(replyTo: ActorRef[List[String]]) extends Command

  case object Subscribe extends Command

  case object Unsubscribe extends Command

  def apply(userName: String, chatRef: ActorRef[ChatGroupActor.Command], aclRef: ActorRef[ACLActor.Command], chatLog: List[String] = List.empty): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      aclRef ! RegisterUser(context.self)

      working(userName, chatRef, aclRef, chatLog)
    }

  private def working(userName: String, chatRef: ActorRef[ChatGroupActor.Command], aclRef: ActorRef[ACLActor.Command], chatLog: List[String] = List.empty): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      Behaviors.receiveMessage {
        case PostMessage(msg) =>
          chatRef ! ChatGroupActor.Message(msg, userName)
          Behaviors.same
        case GetMessage(from, time, msg) =>
          val messageLog: String = s"${time} | ${from}: ${msg}"
          working(userName, chatRef, aclRef: ActorRef[ACLActor.Command], messageLog +: chatLog)
        case GetChatLog(replyTo) =>
          replyTo ! chatLog
          Behaviors.same
        case Subscribe =>
          chatRef ! ChatGroupActor.Subscribe(context.self)
          Behaviors.same
        case Unsubscribe =>
          chatRef ! ChatGroupActor.Unsubscribe(context.self)
          Behaviors.same
      }
    }
}
