package crawler.interface


case class CrawlerSettings[TR <: TaskResult](hooks: CrawlerHooks[TR]) {
  val crawlerName = "default-crawler-name-change-it"
  val tasksParallel = 20
}
