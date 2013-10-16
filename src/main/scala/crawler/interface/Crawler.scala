package crawler.interface

import akka.actor.ActorSystem
import crawler.implementation.{BatchWorker, StartCrawl}


trait Crawler[TR <: TaskResult] {
  val crawlerSettings: CrawlerSettings[TR]

  def start(ts: TasksAndSettings[TR]) {
    require(crawlerSettings != null, "Settings not given")
    val system = ActorSystem(crawlerSettings.crawlerName)
    system.actorOf(BatchWorker.props(ts)) ! StartCrawl
  }
}
