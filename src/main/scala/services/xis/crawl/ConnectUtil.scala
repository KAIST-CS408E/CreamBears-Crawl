package services.xis.crawl

import scala.collection.mutable.{Map => MMap, Buffer}
import scala.collection.JavaConverters

import java.net.{URL, URLEncoder}
import javax.net.ssl.HttpsURLConnection
import java.io.{DataInputStream, DataOutputStream}
import java.util.Scanner

import org.apache.commons.io.IOUtils

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Document

object ConnectUtil {

  type Cookie = MMap[String, String]
  type Result = (Document, MMap[String, Buffer[String]])
  
  private val commonHeader = Map(
    "Connection" -> "close",
    "Content-Type" -> "application/x-www-form-urlencoded"
  )

  private def init(url: String)(implicit cookie: Cookie): HttpsURLConnection = {
    val con = (new URL(url)).openConnection.asInstanceOf[HttpsURLConnection]
    commonHeader.foreach{ case (k, v) => con.setRequestProperty(k, v) }
    con.setRequestProperty("Cookie",
      cookie.map{ case (k, v) => s"${k}=${v}" }.mkString("; "))
    con.setUseCaches(false)
    con.setInstanceFollowRedirects(false)
    con.setDoInput(true)
    con
  }

  private def getCommon(
    url: String, query: Map[String, String] = Map()
  )(implicit cookie: Cookie): HttpsURLConnection = {
    val qStr = query.map{ 
      case (k, v) => s"${k}=${URLEncoder.encode(v, "UTF-8")}"
    }.mkString("&")
    val con = init(s"${url}${if (qStr.isEmpty) "" else "?"}${qStr}")
    con.setRequestMethod("GET")
    con.setDoOutput(false)
    con
  }

  def get(
    url: String, query: Map[String, String] = Map()
  )(implicit cookie: Cookie): Result = res(getCommon(url, query))

  def getRaw(
    url: String, query: Map[String, String] = Map()
  )(implicit cookie: Cookie): Array[Byte] = {
    val con = getCommon(url, query)
    val input = new DataInputStream(con.getInputStream)
    val res = IOUtils.toByteArray(input)
    input.close
    con.disconnect
    res
  }

  def post(
    url: String, form: Map[String, String]
  )(implicit cookie: Cookie): Result = {
    val con = init(url)
    con.setRequestMethod("POST")
    con.setDoOutput(true)

    val output = new DataOutputStream(con.getOutputStream)
    val content = form.map{
      case (k, v) => s"${k}=${URLEncoder.encode(v, "UTF-8")}"
    }.mkString("&")
    output.writeBytes(content)
    output.close

    res(con)
  }

  private def res(con: HttpsURLConnection): Result = {
    val input = new DataInputStream(con.getInputStream)
    val scanner = new Scanner(input, "utf-8").useDelimiter("\\A")
    val content = scanner.next
    val header = JavaConverters.mapAsScalaMap(con.getHeaderFields).map{
      case (k, v) => (k, JavaConverters.asScalaBuffer(v))
    }
    scanner.close
    input.close
    con.disconnect
    (JsoupBrowser().parseString(content), header)
  }
}
