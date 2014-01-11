package org.chepurnoy.crawler.meta.implementation


import scala.collection.mutable
import org.joda.time.DateTime
import org.chepurnoy.crawler.meta._


class TaskWorker[A <: AbstractAddress,
FT <: AbstractFetchingTask[A],
RR,
PTR <: AbstractPartialTaskResult[A],
PR <: AbstractProcessingResult[PTR, FT],
TR <: AbstractTaskResult](override val task: Task[FT, RR, PTR, PR, TR],
                          override val registry: TaskActorsRegistry) extends TaskActor[FT, RR, PTR, PR, TR] with akka.actor.ActorLogging {


  private var openNodes = 0
  private val visited = mutable.Set[A]()

  private def send(fetchingTask: FT) {
    val exists = !visited.add(fetchingTask.address)
    if (exists) {
      println(s"${fetchingTask.address} has already been visited")
    } else {
      openNodes += 1
      registry.fetcherWorker ! fetchingTask
    }
  }

  override def receive = {
    case StartTask =>
      val ft = task.seedTask
      println(s"Starting task with seed address: ${ft.address}; time: ${DateTime.now}")
      send(ft)

    case Message.BulkFetch(fts:List[FT]) =>
      openNodes -= 1
      fts.map(send)

    case Message.NoLinksToFollow =>
      openNodes -= 1
      log.debug(s"Visites: ${visited.size}, open: $openNodes task: $task")
      if (openNodes == 0) {
        println("No links to follow... going to stop the system")
        log.debug(s"Sending taskEnded signal: $task")
        registry.resultsBuilder ! Message.TaskEnded
      }


    case t: Any => println(s"TaskWorker: got strange message! $t")
  }
}
