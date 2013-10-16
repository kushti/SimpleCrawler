package crawler.implementation


import org.joda.time.DateTime
import akka.actor._
import akka.agent.Agent


class PageWorker(taskWorker: ActorRef, stateAgent: Agent[TaskState]) extends Actor  with akka.actor.ActorLogging{
  override def receive = {
    case p: PageTask[_, _] =>
      val task = p.task

      stateAgent send {
        s => s.copy(pagesCrawled = s.pagesCrawled + 1)
      }

      val state = stateAgent()
      println(s"State: $state")

      if (task.maxPages <= Long.MaxValue && state.pagesCrawled >= task.maxPages) {
        sender ! NoLinksToFollow
      } else {
        val url = p.url
        val hooks = task.hooks
        println(s"Going to fetch and parse page: $url")

        if (task.politeness > 0) {
          val now = DateTime.now.getMillis
          val diff = now - state.lastRequestTime
          if (diff < task.politeness) {
            Thread.sleep(task.politeness - diff)
          }
          stateAgent send {
            s => s.copy(lastRequestTime = now)
          }
        }

        val mbRes = try {
          hooks.processPage(p) //todo: pass more appropriate data structure
        } catch {
          case t: Throwable =>
            log.warning(s"processPage function has thrown exception: $t")
            t.printStackTrace()
            None
        }

        val rh = ResultHolder(url, mbRes, task)

        p.pageResActor ! rh
        p.aggResActor ! rh

        mbRes match {
          case Some(res) =>
            if (p.depth < p.task.maxDepth && res.linksToFollow.size > 0) {
              sender ! DepthTask(res.linksToFollow, p.depth + 1)
            } else {
              sender ! NoLinksToFollow
            }
          case None => sender ! NoLinksToFollow
        }
      }
  }
}

object PageWorker {
  def props(tw: ActorRef, state: Agent[TaskState]) = Props(classOf[PageWorker], tw, state)
}


