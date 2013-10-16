package crawler.implementation

import crawler.interface.{Task, TaskResult, PageResult}
import akka.actor.{Actor, Props, ActorRef}
import java.net.URL
import scala.collection.mutable


class TaskResultBuilder[R <: PageResult, AR <: TaskResult](batchWorker: ActorRef, task: Task[R, AR]) extends Actor {
  val m = mutable.Map[URL, Option[R]]()

  def receive = {
    case ResultHolder(url, mbR, _) =>
      mbR match {
        case o: Option[R] =>
          m.put(url, mbR.asInstanceOf[Option[R]]) //todo: a bit ugly code
        case a: Any => println(s"strange! ${classOf[Any]}")
      }

    case TaskEnded =>
      batchWorker ! TaskResultHolder[AR](task.hooks.processTaskResult(task, m.toMap))
  }
}

object TaskResultBuilder {
  def props[R <: PageResult, AR <: TaskResult](aggWorker: ActorRef, task: Task[_, _]) = Props(classOf[TaskResultBuilder[R, AR]], aggWorker, task)
}
