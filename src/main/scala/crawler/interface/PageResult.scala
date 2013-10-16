package crawler.interface

import java.net.URL

trait PageResult {
  val url: URL
  val depth: Int
  val linksToFollow: List[URL]
}
