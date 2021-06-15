lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.12"
    )),
    name := "integration-catalogue-api-platform-tools"
  ).settings(scoverageSettings)

libraryDependencies += "io.swagger.parser.v3" % "swagger-parser-v3" % "2.0.24"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.8"

libraryDependencies += "org.raml" % "webapi-parser" % "0.5.0"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3"

resolvers += "Mulesoft" at "https://repository-master.mulesoft.org/nexus/content/repositories/releases"
resolvers += "jitpack.io" at "https://jitpack.io"



lazy val scoverageSettings = {
    import scoverage.ScoverageKeys
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := ";.*\\.domain\\.models\\..*;uk\\.gov\\.hmrc\\.BuildInfo;.*\\.Routes;.*\\.RoutesPrefix;;Module;GraphiteStartUp;.*\\.Reverse[^.]*",
      ScoverageKeys.coverageMinimum := 70,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
  )
}

