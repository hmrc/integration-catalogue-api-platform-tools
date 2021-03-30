lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.12"
    )),
    name := "scalatest-example"
  )

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"


libraryDependencies += "org.raml" % "webapi-parser" % "0.5.0"

resolvers += "Mulesoft" at "https://repository-master.mulesoft.org/nexus/content/repositories/releases"
resolvers += "jitpack.io" at "https://jitpack.io"
