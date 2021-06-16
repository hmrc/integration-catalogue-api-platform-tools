import sbt._
import sbt.Keys._
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.versioning.SbtGitVersioning

lazy val root = (project in file("."))
  .enablePlugins(SbtGitVersioning, SbtArtifactory)
  .settings(
    scalaVersion := "2.12.12",
    name := "integration-catalogue-api-platform-tools",
    majorVersion := 0
  ).settings(scoverageSettings)

libraryDependencies += "io.swagger.parser.v3" % "swagger-parser-v3" % "2.0.24"
libraryDependencies += "org.raml" % "webapi-parser" % "0.5.0"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.8"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
libraryDependencies += "com.vladsch.flexmark" %  "flexmark-all" % "0.36.8" % Test
libraryDependencies += "org.mockito" %% "mockito-scala-scalatest"  % "1.7.1" % Test

resolvers += "Mulesoft" at "https://repository-master.mulesoft.org/nexus/content/repositories/releases"
resolvers += "jitpack.io" at "https://jitpack.io"

enablePlugins(PackPlugin, SbtAutoBuildPlugin)
packMain := Map("integration-catalogue-api-platform-tools" -> "uk.gov.hmrc.integrationcatalogueapiplatformtools.Main")

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

