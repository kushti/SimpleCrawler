package org.chepurnoy.crawler.meta.implementation

import org.chepurnoy.crawler.meta._
import akka.agent.Agent
import akka.actor.{Actor, ActorLogging}
import org.joda.time.DateTime


class FetcherWorker[FT <: AbstractFetchingTask[_],
RR,
PTR <: AbstractPartialTaskResult[_],
PR <: AbstractProcessingResult[PTR,FT],
TR <: AbstractTaskResult](override val task:Task[FT,RR,PTR,PR,TR],
                          sharedState:Agent[TaskState],
                          override val registry:TaskActorsRegistry) extends TaskActor[FT,RR,PTR,PR,TR] with ActorLogging{
  def receive = {
    case ft: FT =>
      sharedState send {
        s => s.copy(nodesCrawled = s.nodesCrawled + 1)
      }

      val state = sharedState()
      println(s"Shared state: $state")

      if (task.maxNodes <= Long.MaxValue && state.nodesCrawled >= task.maxNodes) {
        registry.taskWorker ! Message.NoLinksToFollow
      } else {
        val address = ft.address

        println(s"Going to fetch: $address")

        if (task.politeness > 0) {
          val now = DateTime.now.getMillis
          val diff = now - state.lastRequestTime
          if (diff < task.politeness) {
            Thread.sleep(task.politeness - diff)
          }
          sharedState send {
            s => s.copy(lastRequestTime = now)
          }
        }

        val mbRes = try {
          task.fetch(ft)
        } catch {
          case t: Throwable =>
            log.warning(s"processPage function has thrown exception: $t")
            t.printStackTrace()
            None
        }

        mbRes match{
          case Some(r) => registry.processingWorker ! Message.Process(r, ft)
          case None => registry.taskWorker ! Message.NoLinksToFollow
        }
      }
  }
}
