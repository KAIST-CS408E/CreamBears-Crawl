package services.xis.crawl

import scala.collection.mutable.{Map => MMap}

import java.nio.file.{Paths, Files}

import org.scalatest._

import ConnectUtil._
import LoginUtil._
import CrawlUtil._
import SearchUtil._

class CrawlSpec extends FlatSpec with Matchers {
  private implicit val cookies: Cookie = MMap()
  private val board = "student_notice"
  private val id = "11537235408097"
  private val title =
    "2018글로벌기술사업화 워크샵 행사 준비를 지원할 학생을 모집합니다."
  private val author = "이백훈"
  private val department = "글로벌기술사업화센터"
  private val time = "2018.09.18 10:50:08"
  private val hits = 238
  private val files =
    List("2018글로벌기술사업화워크샵_지원요원_지원서양식.hwp")
  private val links = List("/board/fileMngr?cmd=down&boardId=student_notice&bltnNo=11537235408097&fileSeq=1&subId=sub06")
  private val content = "글로벌기술사업화센터에서는"
  private val images = List[String]()
  private val keyword = "수강신청"
  private val file = "https://portal.kaist.ac.kr/board/upload/editor/student_notice/T201810231540283340148_png"
  private val filePostfix = "/board/upload/editor/student_notice/T201810231540283340148_png"
  private val specialBoard0 = "work_notice"
  private val specialId0 = "11536792030689"
  private val specialHits0 = 367
  private val specialBoard1 = "seminar_events"
  private val specialId1 = "11540368178139"
  private val specialHits1 = 15
  private val httpFile = "http://www.webworker.co.kr/images/letter/KSW_letter_0603.jpg"

  "Login Config" should "exists" in {
    Files.exists(Paths.get(confPath)) shouldEqual true
  }

  "Login" should "succeeds" in {
    login
    cookies.isDefinedAt("__smVisitorID") shouldEqual true
    cookies.isDefinedAt("EnviewSessionID") shouldEqual true
    cookies.isDefinedAt("evSSOCookie") shouldEqual true
    cookies.isDefinedAt("ObSSOCookie") shouldEqual true
    cookies.isDefinedAt("JSESSIONID") shouldEqual true
  }

  "Max page of the today board" should "be obtained" in {
    getMaxOfToday().size shouldEqual 1
  }

  "Max pages of boards" should "be obtained" in {
    getMax(board).size shouldEqual 1
  }

  "Number of articles per a today board's page" should "be 15" in {
    getSummariesFromToday(1).size shouldEqual 15
  }

  "Number of articles per a page" should "be 15" in {
    getSummaries(board, 1).size shouldEqual 15
  }

  "Articles" should "be obtained correctly" in {
    val article = getArticle(board, id).get
    article.board shouldEqual board
    article.id shouldEqual id
    article.title shouldEqual title
    article.author shouldEqual author
    article.department shouldEqual department
    (article.hits >= hits) shouldEqual true
    article.files shouldEqual files
    article.fileList.get(0) shouldEqual files(0)
    article.links shouldEqual links
    article.linkList.get(0) shouldEqual links(0)
    article.content.substring(0, content.length) shouldEqual content
    article.images shouldEqual images
    article.imageList.size() shouldEqual images.length
  }

  "Search" should "succeeds" in {
    search(keyword, 1).size shouldEqual 10
  }

  "File" should "be obtained" in {
    getFile(file).length shouldEqual 1549752
    getFile(filePostfix).length shouldEqual 1549752
  }

  "Articles in special boards" should "be obtained correctly" in {
    val article0 = getArticle(specialBoard0, specialId0).get
    (article0.hits >= specialHits0) shouldEqual true
    val article1 = getArticle(specialBoard1, specialId1).get
    (article1.hits >= specialHits1) shouldEqual true
  }

  "HTTP file" should "be obtained" in {
    getFile(httpFile).length shouldEqual 119831
  }
}
