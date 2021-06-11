package uk.gov.hmrc.integrationcatalogueapiplatformtools

import amf.client.model.domain.WebApi
import java.io.FileReader
import org.apache.commons.csv.CSVRecord

import scala.collection.JavaConverters._
import webapi.{Oas30, Raml10, WebApiBaseUnit, WebApiDocument}

import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.concurrent.java8.FuturesConvertersImpl
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport
import _root_.repos.GenerateGitCloneBashScript
// $COVERAGE-OFF$
object Main extends App { 

  args.toList match {
    case "--help" :: Nil => println("Print usage instructions")
    case "--generateGitClone" :: Nil => { 
        val linesForBashScript = GenerateGitCloneBashScript.printScript(RepoFileExport.csvApisToProcess())
        println(linesForBashScript)
    }
    case "--generateOas" :: Nil => RepoFileExport.generateOasFiles()
    case unknown => println(s"Unrecognised arguments: $unknown")
  }
}
// $COVERAGE-ON$