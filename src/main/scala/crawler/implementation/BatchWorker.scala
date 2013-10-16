package crawler.implementation

import crawler.interface.{Task, CrawlerHooks, TasksAndSettings, TaskResult}
import akka.actor.{PoisonPill, Actor, Props}
import scala.collection.mutable


//todo: Implement as FSM
class BatchWorker[TR <: TaskResult](ts: TasksAndSettings[TR]) extends Actor with akka.actor.ActorLogging {
  val crawlerHooks: CrawlerHooks[TR] = ts.settings.hooks

  val results = mutable.Buffer[TR]()
  var tasksBuffer = ts.tasks.toBuffer

  var tasksInProcess = 0
  var finishedTasks = 0

  private def sendTasks{
    while(tasksInProcess<ts.settings.tasksParallel && tasksBuffer.nonEmpty){
      self ! tasksBuffer.head
      tasksBuffer trimStart 1
      tasksInProcess += 1
    }
  }

  def receive = {
    case StartCrawl =>
      log.debug(s"Going to process task list with size ${ts.tasks.size}")
      sendTasks

    case task:Task[_,_] =>
      val taskActor = context.actorOf(Props(classOf[TaskWorker], self, task))
      taskActor ! StartTask

    case TaskResultHolder(r: TR) =>
      tasksInProcess -= 1
      sendTasks

      finishedTasks += 1
      results += r

      if (ts.tasks.size == finishedTasks) {
        log.debug("Whole parsing finished")
        crawlerHooks.processBatchResult(results.toList)
        self ! PoisonPill
      }
  }
}

object BatchWorker {
  def props[TR <: TaskResult](ts: TasksAndSettings[TR]) = Props(classOf[BatchWorker[TR]], ts)
}
