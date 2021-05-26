package uk.gov.hmrc.integrationcatalogueapiplatformtools

import _root_.repos.GenerateGitCloneBashScript
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.webapi.{Oas30Plugin, Raml10Plugin}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport
import scala.compat.java8.FutureConverters._

object Main extends App {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  import amf.core.AMF

//  amf.core.AMF.registerPlugin(AMLPlugin)
//  amf.core.AMF.registerPlugin(Oas30Plugin)
//  amf.core.AMF.registerPlugin(Raml10Plugin)
//  amf.core.AMF.init()

  import amf.Core
  import amf.plugins.document.WebApi


  WebApi.register()
  Core.registerPlugin(Oas30Plugin)
  Core.registerPlugin(Raml10Plugin)
  Core.init().get

  args.toList match {
    case "--help" :: Nil => println("Print usage instructions")
    case "--generateGitClone" :: Nil => GenerateGitCloneBashScript.printScript(RepoFileExport.csvApisToProcess())
    case "--generateOas" :: Nil => RepoFileExport.generateOasFiles()
    case unknown => println(s"Unrecognised arguments: $unknown")
  }



}
