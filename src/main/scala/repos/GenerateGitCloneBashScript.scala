package repos

import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.CsvApiRecord

object GenerateGitCloneBashScript {
  def printScript(apisToProcess: Seq[CsvApiRecord]) = {

    val text = apisToProcess
      .map(api => s"git clone git@github.com:hmrc/${api.name}.git")
      .mkString("\n")
  
    println(text)
  }
}
