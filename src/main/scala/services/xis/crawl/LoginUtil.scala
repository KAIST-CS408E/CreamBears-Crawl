package services.xis.crawl

import scala.collection.mutable.Buffer
import scala.io.Source

import ConnectUtil._

object LoginUtil {
  val confPath = "login.conf"
  private val loginUrl = "https://portalsso.kaist.ac.kr/ssoProcess.ps"
  private val keyCookie = "Set-Cookie"
  private val keyLocation = "Location"

  def login(implicit cookie: Cookie): Unit =
    readFile(confPath).foreach(s => s.split("\n").toList match {
      case id :: pw :: Nil =>
        val userInfo = Map("userId" -> id, "password" -> pw)
        val res = post(loginUrl, userInfo)
        cookie ++= readCookie(res)
        cookie ++= readCookie(get(readRedirection(res)))
      case _ =>
    })

  private def readCookie(res: Result): Buffer[(String, String)] =
    res._2(keyCookie).map(s => {
      val i = s.indexOf(";")
      val s1 = if (i == -1) s else s.substring(0, i)
      val i1 = s1.indexOf("=")
      if (i1 == -1) (s1, "") else (s1.substring(0, i1), s1.substring(i1 + 1))
    })

  private def readRedirection(res: Result): String = res._2(keyLocation).head

  private def readFile(name: String): Option[String] = 
    try {
      Some(Source.fromFile(name, "UTF-8").mkString)
    } catch {
      case _: Exception => None
    }
}
