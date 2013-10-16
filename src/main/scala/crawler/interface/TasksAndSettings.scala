package crawler.interface


case class TasksAndSettings[TR <: TaskResult](settings: CrawlerSettings[TR], tasks: List[Task[_ <: PageResult, TR]])
