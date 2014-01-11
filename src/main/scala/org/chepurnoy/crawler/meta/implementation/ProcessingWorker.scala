package org.chepurnoy.crawler.meta.implementation

import org.chepurnoy.crawler.meta._
import akka.agent.Agent
import akka.actor.ActorLogging

class ProcessingWorker [FT <: AbstractFetchingTask[_],
                        RR,
                        PTR <: AbstractPartialTaskResult[_],
                        PR <: AbstractProcessingResult[PTR, FT],
                        TR <: AbstractTaskResult]
                       (override val task: Task[FT, RR, PTR, PR, TR],
                        sharedState: Agent[TaskState],
                        override val registry: TaskActorsRegistry)
                      extends TaskActor[FT, RR, PTR, PR, TR] with ActorLogging {
  def receive = {
    case Message.Process(r: RR, ft: FT) =>
      println(s"Going to process: ${ft.address}")
      val res = task.process(r, ft)

      println(s"New tasks created for ${ft.address}: ${res.toFollow.size}")
      if (ft.depth < task.maxDepth && res.toFollow.size > 0) {
        registry.taskWorker ! Message.BulkFetch(res.toFollow)
      } else {
        registry.taskWorker ! Message.NoLinksToFollow
      }
      registry.resultsBuilder ! res.partialTaskResult
  }
}
