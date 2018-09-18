# kaist-portal-crawl

* How to test
```shell
$ git clone https://github.com/CreamBears/kaist-portal-crawl.git
$ cd kaist-portal-crawl
$ echo [id] > login.conf
$ echo [password] >> login.conf
$ sbt test
```
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
