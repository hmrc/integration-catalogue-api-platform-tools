package uk.gov.hmrc.integrationcatalogueapiplatformtools

import _root_.repos.GenerateGitCloneBashScript
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport

object Main extends App {

  import amf.core.AMF

  AMF.init.value
  args.toList match {
    case "--help" :: Nil => println("Print usage instructions")
    case "--generateGitClone" :: Nil => GenerateGitCloneBashScript.printScript(RepoFileExport.csvApisToProcess())
    case "--generateOas" :: Nil => RepoFileExport.generateOasFiles()
    case unknown => println(s"Unrecognised arguments: $unknown")
  }
}
