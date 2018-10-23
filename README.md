# kaist-portal-crawl

## How to use
* Add the following line to your `project/Dependencies` file.
```scala
lazy val crawler = RootProject(uri("git://github.com/CreamBears/kaist-portal-crawl.git"))
```
* Modify your `build.sbt` file like the following:
```scala
lazy val root = (project in file("."))
  .dependsOn(crawler)
  .settings(...)
```
* Create `login.conf` file.
```shell
$ echo [id] > login.conf
$ echo [password] >> login.conf
```
* Example
```scala
import scala.collection.mutable.{Map => MMap}

import ConnectUtil._
import LoginUtil._
import CrawlUtil._
import SearchUtil._

implicit val cookies: Cookie = MMap() // cookies

login // login

val max: Int = getMaxOfToday().get // Max page of today_notice
val ids: List[(String, String)] = getIdsFromToday(1) // Page 1 of today_notice
val board: String = ids(0)._1 // Board of the latest article
val id: String = ids(0)._2 // Id of the latest article
val article: Article = getArticle(board, id).get // The latest article

val link = "https://some.url"
val data: Array[Byte] = getRaw(link) // Data of the file
```

## How to test
```shell
$ git clone https://github.com/CreamBears/kaist-portal-crawl.git
$ cd kaist-portal-crawl
$ echo [id] > login.conf
$ echo [password] >> login.conf
$ sbt test
```

## API
* Article class
```Java
String board();
String id();
String title();
String author();
String department();
String time();
int hits();
java.util.List<String> fileList();
java.util.List<String> linkList();
String content();
java.util.List<String> imageList();
```

* SearchResult class
```Java
String title();
String preview();
String link();
String board();
String id();
```
