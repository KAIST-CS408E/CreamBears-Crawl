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
