package crawler.implementation
import crawler.interface.{Task, TaskResult, PageResult}
import java.net.URL
import akka.actor.ActorRef


case object NoLinksToFollow

case object StartCrawl

case object StartTask

case object TaskEnded

case object AggregatedResultProcessed

case class TaskState(pagesCrawled: Int, lastRequestTime: Long)

protected case class TaskResultHolder[AR <: TaskResult](res: AR)

case class DepthTask(urls: List[URL], depth: Int)

protected case class ResultHolder[R <: PageResult, AR <: TaskResult](url: URL, result: Option[R], task: Task[R, AR])

case class PageTask[R <: PageResult, AR <: TaskResult](task: Task[R, AR], pageResActor: ActorRef,
                                                       aggResActor: ActorRef, url: URL, depth: Int)






