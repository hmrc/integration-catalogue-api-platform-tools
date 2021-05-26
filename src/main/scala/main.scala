package uk.gov.hmrc.integrationcatalogueapiplatformtools

import _root_.repos.GenerateGitCloneBashScript
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.webapi.{Oas30Plugin, Raml10Plugin}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport


object Main extends App {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  import amf.core.AMF

//  amf.core.AMF.registerPlugin(AMLPlugin)
//  amf.core.AMF.registerPlugin(Oas30Plugin)
//  amf.core.AMF.registerPlugin(Raml10Plugin)
//  amf.core.AMF.init()

  AMF.registerPlugin(Oas30Plugin)
  AMF.registerPlugin(Raml10Plugin)
  AMF.init()
 .map({something: Unit =>
    args.toList match {
      case "--help" :: Nil => println("Print usage instructions")
      case "--generateGitClone" :: Nil => GenerateGitCloneBashScript.printScript(RepoFileExport.csvApisToProcess())
      case "--generateOas" :: Nil => RepoFileExport.generateOasFiles()
      case unknown => println(s"Unrecognised arguments: $unknown")
    }
  })


}
