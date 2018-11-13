package services.xis.crawl

import ConnectUtil._

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

object SearchUtil {
  private val url = "https://search.kaist.ac.kr/index.jsp"

  private def search1(
    doc: Document, index: Int
  )(implicit cookie: Cookie): List[SearchResult] =
    doc >> elementList(".section_body") match {
      case body :: _ =>
        val subjects = (body >> elementList(".subject"))
          .flatMap(_ >> elementList("a"))
        val previews = body >> elementList(".cont_txt") >> allText("span")
        (subjects zip previews).map {
          case (s, p) =>
            val title = s >> allText("a")
            val link = s >> attr("href")("a")
            val end = link.lastIndexOf("/")
            val start = link.lastIndexOf("/", end - 1)
            SearchResult(title, p, link,
              link.substring(start + 1, end), link.substring(end + 1))
        }
      case Nil => List()
    }

  def search(
    keyword: String, index: Int
  )(implicit cookie: Cookie): List[SearchResult] =
    search1(
      get(url, Map("searchTerm" -> keyword,
        "searchTarget" -> "portal", "currentPage" -> index.toString))._1,
      index
    )

  def search(
    keyword: String, start: String, end: String, index: Int
  )(implicit cookie: Cookie): List[SearchResult] =
    search1(
      get(url, Map("searchTerm" -> keyword,
        "searchTarget" -> "portal", "currentPage" -> index.toString,
        "searchDate" -> "input", "searchStartDate" -> start,
        "searchEndDate" -> end))._1,
      index
    )
}
