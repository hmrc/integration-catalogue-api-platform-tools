package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos



object GenerateGitCloneBashScript {
  def printScript(apisToProcess: Seq[CsvApiRecord]): String = {
    apisToProcess
      .map(api => s"git clone git@github.com:hmrc/${api.name}.git")
      .mkString("\n")
  }
}
