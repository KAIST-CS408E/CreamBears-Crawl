# xIS-crawler

## How to use
* Add the following line to your `project/Dependencies` file.
```scala
lazy val crawler = RootProject(uri("git://github.com/KAIST-CS408E/CreamBears-Crawl.git"))
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
val ids: List[ArticleSummary] = getSummariesFromToday(1) // Page 1 of today_notice
val board: String = ids(0).board // Board of the latest article
val id: String = ids(0).id // Id of the latest article
val hits: String = ids(0).hits // Hits of the latest article
val article: Article = getArticle(board, id).get // The latest article

val link = "https://portal.kaist.ac.kr/some/file"
val data: Array[Byte] = getFile(link) // Data of the file
```

## How to test
```shell
$ git clone https://github.com/KAIST-CS408E/CreamBears-Crawl.git
$ cd CreamBears-Crawl
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
