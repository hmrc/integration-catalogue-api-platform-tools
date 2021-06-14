package uk.gov.hmrc.integrationcatalogueapiplatformtools


import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos._
import uk.gov.hmrc.integrationcatalogueapiplatformtools.csv.CsvUtils
// $COVERAGE-OFF$
object Main extends App { 

  args.toList match {
    case "--help" :: Nil => println("Print usage instructions")
    case "--generateGitClone" :: Nil =>
      val linesForBashScript = GenerateGitCloneBashScript.printScript(CsvUtils.csvApisToProcess("api-definition-csv-export.csv"))
      println(linesForBashScript)
    case "--generateOas" :: Nil => RepoFileExport.generateOasFiles("api-definition-csv-export.csv")
    case unknown => println(s"Unrecognised arguments: $unknown")
  }
}
// $COVERAGE-ON$