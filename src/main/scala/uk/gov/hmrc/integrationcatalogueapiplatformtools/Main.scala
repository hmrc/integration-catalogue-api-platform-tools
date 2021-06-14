package uk.gov.hmrc.integrationcatalogueapiplatformtools


import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos._
// $COVERAGE-OFF$
object Main extends App { 

  args.toList match {
    case "--help" :: Nil => println("Print usage instructions")
    case "--generateGitClone" :: Nil =>
      val linesForBashScript = GenerateGitCloneBashScript.printScript(RepoFileExport.csvApisToProcess())
      println(linesForBashScript)
    case "--generateOas" :: Nil => RepoFileExport.generateOasFiles()
    case unknown => println(s"Unrecognised arguments: $unknown")
  }
}
// $COVERAGE-ON$