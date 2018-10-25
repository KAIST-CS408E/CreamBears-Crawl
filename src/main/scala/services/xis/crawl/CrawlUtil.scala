package services.xis.crawl

import ConnectUtil._

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

object CrawlUtil {

  private val boardUrl = "https://portal.kaist.ac.kr/board/list.brd"
  private val articleUrl = "https://portal.kaist.ac.kr/board/read.brd"
  private val todayBoard = "today_notice"
  private val fileUrl = "https://portal.kaist.ac.kr"
  private val fileUrlPrefix = "http"

  private def getBoard(
    board: String, index: Int
  )(implicit cookie: Cookie): Document = {
    post(boardUrl, Map("page" -> index.toString, "boardId" -> board))._1
  }

  def getMax(board: String)(implicit cookie: Cookie): Option[Int] = {
    val doc = getBoard(board, 1)
    val str = (doc >> elementList("script"))
      .map(_.innerHtml).mkString("").split("\n")
      .filter(_.contains("var totalPage")).toList.head
    val start = str.indexOf("= ")
    if (start < 0) None
    else {
      val end = str.indexOf(";")
      val num = """(\d+)""".r
      str.substring(start + 2, end) match {
        case num(max) => Some(max.toInt)
        case _ => None
      }
    }
  }

  def getMaxOfToday()(implicit cookie: Cookie): Option[Int] =
    getMax(todayBoard)

  def getSummaries(
    board: String, index: Int
  )(implicit cookie: Cookie): List[ArticleSummary] = {
    val doc = getBoard(board, index)

    val key = "/ennotice/"
    def parse(s: String): (String, String) = {
      val s0 = s.substring(key.length)
      val i = s0.indexOf("/")
      (s0.substring(0, i), s0.substring(i + 1))
    }

    val bids = (doc >> elementList("a") >?> (_ >> attr("href")("a")))
      .flatten.filter(_.contains(key)).map(parse)
    val hitsList = (doc >> elementList("tr"))
      .map(_ >> elementList("td"))
      .filter(_.length == 5)
      .map(_(3) >> allText("td"))

    (bids zip hitsList).map{
      case ((b, id), hits) => ArticleSummary(b, id, hits.toInt)
    }
  }

  def getSummariesFromToday(index: Int)
    (implicit cookie: Cookie): List[ArticleSummary] =
    getSummaries(todayBoard, index)

  def getArticle(
    board: String, id: String
  )(implicit cookie: Cookie): Option[Article] = {
    val doc = get(articleUrl,
      Map("cmd" -> "READ", "boardId" -> board, "bltnNo" -> id))._1

    def parse(au: String) = {
      val (start, end) = (au.indexOf('('), au.indexOf(')'))
      if (start < 0 || end < 0) (au, "")
      else (au.substring(0, start), au.substring(start + 1, end))
    }
    def makeArticle(
      tit: Element, aut: Element, tim: Element, att: Element, con: Element
    ): Article = {
      val title = tit >> allText("td")
      val (author, department) = parse(aut >> allText("td"))
      val (time, hits) = parse(tim >> allText("td"))
      val files =
        (att >> elementList(".req_file") >?> allText("a")).flatten
      val links =
        (att >> elementList(".req_file") >?> attr("href")("a")).flatten
      val content = con >> allText("td")
      val images = (con >> elementList("img") >?> attr("src")("img")).flatten
      Article(board, id, title, author, department,
        time, hits.toInt, files, links, content, images)
    }

    val special = Set("work_notice", "seminar_events")
    (doc >> elementList("tbody")).head >> elementList("td") match {
      case tit :: _ :: _ :: aut :: tim :: att :: con :: _ if special(board) =>
        Some(makeArticle(tit, aut, tim, att, con))
      case tit :: aut :: tim :: att :: con :: _ =>
        Some(makeArticle(tit, aut, tim, att, con))
      case _ => None
    }
  }

  def getFile(path: String)(implicit cookie: Cookie): Array[Byte] =
    getRaw(
      if (path startsWith fileUrlPrefix) path
      else s"$fileUrl$path"
    )
}
