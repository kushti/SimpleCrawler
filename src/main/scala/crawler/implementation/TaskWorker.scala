package crawler.implementation

import akka.actor.{Props, Actor, ActorRef}
import crawler.interface.{TaskResult, PageResult, Task}
import akka.agent.Agent
import scala.collection.mutable
import akka.routing.RoundRobinRouter
import java.net.URL
import org.joda.time.DateTime


class TaskWorker(listWorker: ActorRef, task: Task[_ <: PageResult, _ <: TaskResult]) extends Actor with akka.actor.ActorLogging{
   import scala.concurrent.ExecutionContext.Implicits.global

   val stateAgent = Agent(TaskState(0, 0))
   private val router = context.actorOf(PageWorker.props(self, stateAgent).
     withRouter(RoundRobinRouter(nrOfInstances = task.pagesParallel)))

   val aggResActor = context.actorOf(TaskResultBuilder.props(listWorker, task))
   val pageResActor = context.actorOf(Props[PageResultWorker])

   private var openNodes = 0
   private val visitedUrls = mutable.Set[URL]()

   private def send(pageResActor: ActorRef, aggResActor: ActorRef, url: URL, depth: Int) {
     val exists = !visitedUrls.add(url)
     if (exists) {
       println(s"$url has already been visited")
     } else {
       openNodes += 1
       router ! PageTask(task, pageResActor, aggResActor, url, depth)
     }
   }

   def receive = {
     case StartTask =>
       println(s"Starting task with seed url: ${task.seedUrl}; time: ${DateTime.now}")
       send(pageResActor, aggResActor, task.seedUrl, 1)

     case DepthTask(urls, depth) =>
       openNodes -= 1
       urls.map(send(pageResActor, aggResActor, _, depth))

     case NoLinksToFollow =>
       openNodes -= 1
       log.debug(s"Visites: ${visitedUrls.size}, open: $openNodes task: $task")
       if (openNodes == 0) {
         log.debug(s"Sending taskEnded signal: $task")
         aggResActor ! TaskEnded
       }

     case AggregatedResultProcessed =>
       log.debug(s"End Of Task: $task")
       listWorker ! TaskEnded
   }
 }
