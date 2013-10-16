package crawler.interface

trait CrawlerHooks[TR <: TaskResult] {
  def processBatchResult(results: List[TR]): Unit
}
