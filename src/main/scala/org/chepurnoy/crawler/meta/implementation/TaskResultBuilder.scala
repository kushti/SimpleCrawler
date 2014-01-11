package org.chepurnoy.crawler.meta.implementation

import akka.actor.{ActorRef, Actor}
import scala.collection.mutable
import org.chepurnoy.crawler.meta._


//todo: more generic implementation

class TaskResultBuilder[FT <: AbstractFetchingTask[_],
RR,
PTR <: AbstractPartialTaskResult[_],
PR <: AbstractProcessingResult[PTR,FT],
TR <: AbstractTaskResult](override val task:Task[FT,RR,PTR,PR,TR],
                        override val registry:TaskActorsRegistry) extends TaskActor[FT,RR,PTR,PR,TR] {
  val m = mutable.Buffer[PTR]()

  override def receive = {
    case p:PTR => m += p

    case Message.TaskEnded =>
      registry.batchWorker ! task.buildTaskResult(m.toList)
  }
}