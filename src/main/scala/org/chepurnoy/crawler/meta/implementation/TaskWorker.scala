package org.chepurnoy.crawler.meta.implementation


import scala.collection.mutable
import org.joda.time.DateTime
import org.chepurnoy.crawler.meta._
import scala.concurrent.duration._
import play.api.libs.iteratee.Iteratee


case object CheckTask


class TaskWorker[A <: AbstractAddress,
FT <: AbstractFetchingTask[A],
RR,
PTR <: AbstractPartialTaskResult[A],
PR <: AbstractProcessingResult[PTR, FT],
TR <: AbstractTaskResult](override val task: Task[FT, RR, PTR, PR, TR],
                          override val registry: TaskActorsRegistry) extends TaskActor[FT, RR, PTR, PR, TR] with akka.actor.ActorLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  private var openNodes = 0
  private val visited = mutable.Set[A]()
  private var lastSeedTime = new DateTime()

  private def send(fetchingTask: FT) {
    val exists = !visited.add(fetchingTask.address)
    if (exists && !task.revisitAllowed) {
      println(s"${fetchingTask.address} has already been visited")
    } else {
      openNodes += 1
      registry.fetcherWorker ! fetchingTask
    }
  }

  override def receive = {
    case StartTask =>
      val iterator = Iteratee.foreach[FT]{ft=>
        println(s"Starting with seed address: ${ft.address}; time: ${DateTime.now}")
        lastSeedTime = new DateTime()
        send(ft)
      }

      task.seeds.run(iterator)

      context.system.scheduler.schedule(30 seconds, 30 seconds)(self ! CheckTask)

    case CheckTask =>
      log.debug(s"Visites: ${visited.size}, open: $openNodes task: $task")
      /*if (openNodes == 0) {
        println("No links to follow... going to stop the crawling task")
        log.debug(s"Sending taskEnded signal: $task")
        registry.resultsBuilder ! Message.TaskEnded
      } */

      val curMillis = new DateTime().getMillis

      if((curMillis - lastSeedTime.getMillis) > task.timeout.toMillis){
        println("Timeout happened... going to stop the crawling task")
      }

    case Message.BulkFetch(fts:List[FT]) =>
      openNodes -= 1
      fts.map(send)

    case Message.NoLinksToFollow =>
      openNodes -= 1

    case t: Any => println(s"TaskWorker: got strange message! $t")
  }
}
