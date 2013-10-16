package crawler.interface

import java.net.URL
import crawler.implementation.PageTask


trait TaskHooks[PR <: PageResult, TR <: TaskResult] {
  def processPage(p: PageTask[PR, TR]): Option[PR]
  def processPageResult(r: PR)
  def processTaskResult(task: Task[_,_] ,results: Map[URL, Option[PR]]): TR
}
