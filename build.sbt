import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "services.xis.crawl",
      scalaVersion := "2.12.6",
      version      := "1.0.0"
    )),
    name := "kaist-portal-crawl",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.1.0",
    libraryDependencies += "commons-io" % "commons-io" % "2.6"
  )
