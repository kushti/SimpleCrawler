package crawler.interface

import java.net.URL
import scala.concurrent.duration._

case class Task[R <: PageResult, TR <: TaskResult](seedUrl: URL, hooks: TaskHooks[R, TR]) {
  //pause in millis between requests
  val politeness: Short = 0

  //how many pages to fetch and process simultaneously
  val pagesParallel = 10

  //max depth from seed URL to be reached
  val maxDepth = 2

  //max pages to fetch for a task
  val maxPages = Long.MaxValue

  //todo:implement ??
  val ignoreQueryParams = false

  //todo:implement  todo: client level?
  val allowSeedUrlRedirect = true

  //todo:implement ??
  val timeout = 10 minutes
}
