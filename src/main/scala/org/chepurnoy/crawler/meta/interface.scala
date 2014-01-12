package org.chepurnoy.crawler.meta

import akka.actor.ActorSystem
import scala.concurrent.duration._
import org.chepurnoy.crawler.meta.implementation.{StartCrawl, BatchWorker}
import play.api.libs.iteratee.Enumerator

//task level

trait AbstractAddress

trait AbstractFetchingTask[AT <: AbstractAddress]{
  type Address = AT

  val address: AT
  val depth:Int
}


trait AbstractPartialTaskResult[AT <: AbstractAddress]{
  val address: AT
}

trait AbstractProcessingResult[PT <: AbstractPartialTaskResult[_], FT <: AbstractFetchingTask[_]]{
  val partialTaskResult: PT
  val toFollow: List[FT]
}

trait AbstractRawResult{
}

trait AbstractTaskResult


//graph traverse task
trait Task[FetchingTask <: AbstractFetchingTask[_],
           RawResult,
           PartialTaskResult <: AbstractPartialTaskResult[_],
           ProcessingResult <: AbstractProcessingResult[PartialTaskResult, FetchingTask],
           TaskResult <: AbstractTaskResult]{

  val politeness: Short = 0 //pause in millis between requests

  val fetchingParallel = 10 //how many pages to fetch simultaneously
  val processParallel = 2 //how many pages to process simultaneously

  val maxDepth = 3 //max depth from seed URL to be reached
  val maxNodes = Long.MaxValue //max pages to fetch for a task

  val revisitAllowed = false

  val timeout = 1 minute //time from the last seed has been submitted
  def seeds: Enumerator[FetchingTask]

  def fetch(task: FetchingTask): Option[RawResult]
  def process(r: RawResult, fetchingTask:FetchingTask):ProcessingResult
  def buildTaskResult(results: List[PartialTaskResult]): TaskResult
}

//Crawler level

trait CrawlerHooks{
  def processBatchResult(results: List[_ <: AbstractTaskResult]): Unit
}

case class CrawlerSettings(hooks: CrawlerHooks) {
  val crawlerName = "default-crawler-name-change-it"
  val tasksParallel = 20
}

case class TasksAndSettings(settings: CrawlerSettings, tasks: List[Task[_,_,_,_,_]])

trait Crawler{
  val crawlerSettings: CrawlerSettings
  val tasks: List[Task[_,_,_,_,_]]

  def start{
    require(crawlerSettings != null, "Settings not given")
    val system = ActorSystem(crawlerSettings.crawlerName)
    system.actorOf(BatchWorker.props(TasksAndSettings(crawlerSettings, tasks))) ! StartCrawl
  }
}