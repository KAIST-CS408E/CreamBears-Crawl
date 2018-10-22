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

  private def getIdsCommon[T](
    board: String, index: Int, key: String, cb: String => T
  )(implicit cookie: Cookie): List[T] = {
    val doc = getBoard(board, index)
    (doc >> elementList("a") >?> (_ >> attr("href")("a")))
      .flatten.filter(_.contains(key)).map(cb)
  }

  def getIds(
    board: String, index: Int
  )(implicit cookie: Cookie): List[String] = {
    val key = s"/ennotice/${board}/"
    getIdsCommon(board, index, key,
      s => s.substring(key.length, s.length))
  }

  def getIdsFromToday(
    index: Int
  )(implicit cookie: Cookie): List[(String, String)] = {
    val key = "/ennotice/"
    getIdsCommon(todayBoard, index, key, s => {
      val s0 = s.substring(key.length)
      val i = s0.indexOf("/")
      (s0.substring(0, i), s0.substring(i + 1))
    })
  }

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
    (doc >> elementList("tbody")).head >> elementList("td") match {
      case tit :: aut :: tim :: att :: con :: _ =>
        val title = tit >> allText("td")
        val (author, department) = parse(aut >> allText("td"))
        val (time, hits) = parse(tim >> allText("td"))
        val files =
          (att >> elementList(".req_file") >?> allText("a")).flatten
        val links =
          (att >> elementList(".req_file") >?> attr("href")("a")).flatten
        val content = con >> allText("td")
        val images = (con >> elementList("img") >?> attr("src")("img")).flatten
        Some(Article(board, id, title, author, department,
          time, hits.toInt, files, links, content, images))
      case _ => None
    }
  }
}
