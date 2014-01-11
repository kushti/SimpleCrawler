package org.chepurnoy.crawler.meta.implementation

import akka.actor.{PoisonPill, Actor, Props}
import scala.collection.mutable
import org.chepurnoy.crawler.meta._
import akka.agent.Agent
import scala.concurrent.ExecutionContext.Implicits.global
import akka.routing.RoundRobinRouter
import akka.routing


//todo: Implement as FSM?
class BatchWorker(ts: TasksAndSettings) extends Actor with akka.actor.ActorLogging {
  val crawlerHooks: CrawlerHooks = ts.settings.hooks

  val results = mutable.Buffer[AbstractTaskResult]()
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

    case task:Task[_,_,_,_,_] =>
      val stateAgent = Agent(TaskState(0, 0l))
      val registry = new TaskActorsRegistry

      val resultsBuilder = context.actorOf(Props(classOf[TaskResultBuilder[_,_,_,_,_]], task, registry))
      val fetcherWorker = context.actorOf(Props(classOf[FetcherWorker[_,_,_,_,_]], task, stateAgent, registry).
                        withRouter(RoundRobinRouter(task.fetchingParallel)))

      val processingWorker = context.actorOf(Props(classOf[ProcessingWorker[_,_,_,_,_]], task, stateAgent, registry).
        withRouter(RoundRobinRouter(task.processParallel)))

      val taskWorker = context.actorOf(Props(classOf[TaskWorker[_,_,_,_,_,_]], task, registry))

      registry.batchWorker = self
      registry.processingWorker = processingWorker
      registry.fetcherWorker = fetcherWorker
      registry.resultsBuilder = resultsBuilder
      registry.taskWorker = taskWorker

      taskWorker ! StartTask

    case r:AbstractTaskResult =>
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
  def props(ts: TasksAndSettings) = Props(classOf[BatchWorker], ts)
}
