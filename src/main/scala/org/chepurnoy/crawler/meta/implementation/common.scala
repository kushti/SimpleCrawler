package org.chepurnoy.crawler.meta.implementation

import akka.actor.{Actor, ActorRef}
import akka.agent.Agent
import org.chepurnoy.crawler.meta._
import scala.reflect.ClassTag
import scala.collection.mutable


case object StartCrawl

protected case object StartTask
protected case class TaskState(nodesCrawled: Int, lastRequestTime: Long)

class TaskActorsRegistry {
  var batchWorker: ActorRef = null
  var taskWorker: ActorRef = null

  var fetcherWorker: ActorRef = null
  var processingWorker: ActorRef = null

  var resultsBuilder: ActorRef = null
}


object Message {
  case class Fetch[FetchingTask <: AbstractFetchingTask[_]](ft:FetchingTask)
  case class BulkFetch[FetchingTask <: AbstractFetchingTask[_]](fts: List[FetchingTask])
  case class Process[RawResult, FetchingTask <: AbstractFetchingTask[_]](r: RawResult, parent: FetchingTask)
  case object NoLinksToFollow
  case object TaskEnded
}


protected trait TaskActor[FetchingTask <: AbstractFetchingTask[_],
                          RawResult,
                          PartialTaskResult <: AbstractPartialTaskResult[_],
                          ProcessingResult <: AbstractProcessingResult[PartialTaskResult, FetchingTask],
                          TaskResult <: AbstractTaskResult] extends Actor {

  protected val task: Task[FetchingTask, RawResult, PartialTaskResult, ProcessingResult, TaskResult]
  val registry: TaskActorsRegistry
}





