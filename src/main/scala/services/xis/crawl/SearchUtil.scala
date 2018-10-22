package services.xis.crawl

import ConnectUtil._

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model._

object SearchUtil {
  private val url = "https://search.kaist.ac.kr/index.jsp"

  def search(
    keyword: String, index: Int
  )(implicit cookie: Cookie): List[SearchResult] = {
    val doc = get(url, Map("searchTerm" -> keyword,
      "searchTarget" -> "portal", "currentPage" -> index.toString))._1
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
  }
}
