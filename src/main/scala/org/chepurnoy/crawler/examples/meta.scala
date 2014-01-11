package org.chepurnoy.crawler.examples

import org.chepurnoy.crawler.meta._
import com.github.theon.uri.Uri
import scala.concurrent.{ExecutionContext, Future}
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import ExecutionContext.Implicits.global
import scala.collection.JavaConversions._


class Url(url:String) extends AbstractAddress{
  def get = url
  def getAsUri = Uri.stringToUri(url)

  override def toString = url
}

case class HttpFetchingTask(url:String,
                       override val depth:Int,
                       parentTask:Option[HttpFetchingTask]) extends AbstractFetchingTask[Url]{
  override val address = new Url(url)
}

abstract class JsoupTask[PartialTaskResult <: AbstractPartialTaskResult[Url],
                         ProcessingResult <: AbstractProcessingResult[PartialTaskResult, HttpFetchingTask],
                         TaskResult <: AbstractTaskResult](seedUrl:String)
  extends Task[HttpFetchingTask, Document, PartialTaskResult, ProcessingResult,TaskResult]{

  override val maxNodes = 50l

  def seedTask = new HttpFetchingTask(seedUrl, 0, None)
  def fetch(task: HttpFetchingTask): Option[Document] = Option(Jsoup.connect(task.address.get).get())
}

class TaskResult(res:Boolean) extends AbstractTaskResult
class ExamplePartialTaskResult(override val address:Url, result:Boolean) extends AbstractPartialTaskResult[Url]

class ExampleProcessingResult(override val partialTaskResult:ExamplePartialTaskResult,
                              override val toFollow:List[HttpFetchingTask],
                              parentTask:HttpFetchingTask)
  extends AbstractProcessingResult[ExamplePartialTaskResult, HttpFetchingTask]

class ExampleTaskResult extends AbstractTaskResult

class JsoupExampleTask(seedUrl:String) extends JsoupTask[ExamplePartialTaskResult,ExampleProcessingResult,ExampleTaskResult](seedUrl){
  def process(doc: Document, fetchingTask: HttpFetchingTask) = {
    println(s"Processing: ${fetchingTask.address} ${doc.text()}")
    val outLinks = doc.select("a[href]").
      iterator().
      toList.
      map(_.attr("abs:href"))

    val newTasks = outLinks.map{l=>
      new HttpFetchingTask(l, fetchingTask.depth+1, Some(fetchingTask))
    }

    val partialResult = new ExamplePartialTaskResult(fetchingTask.address, true)
    new ExampleProcessingResult(partialResult, newTasks, fetchingTask)
  }

  def buildTaskResult(results: List[ExamplePartialTaskResult]) = new ExampleTaskResult
}


object MetaExampleCrawler extends Crawler with App{
  val tasks = List(new JsoupExampleTask("http://en.wordpress.com/fresh/"))

  val crawlerSettings = new CrawlerSettings(new CrawlerHooks() {
    def processBatchResult(results: List[_ <: AbstractTaskResult]): Unit = {
      println("Whole crawling is done")
    }
  }) {
    override val crawlerName = "WordpressCrawler"
  }

  start
}

