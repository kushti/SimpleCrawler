package crawler.implementation

import akka.actor.Actor


class PageResultWorker extends Actor {
  def receive = {
    case ResultHolder(url, mbR, task) =>
      mbR.map(task.hooks.processPageResult)
  }
}
