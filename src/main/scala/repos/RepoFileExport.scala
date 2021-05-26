package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import amf.client.model.domain.WebApi

import java.io.FileReader
import org.apache.commons.csv.CSVRecord

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import amf.client.model.document.BaseUnit
import amf.client.model.document.Document
import amf.client.model.domain.DomainElement
import amf.client.parse._
import amf.client.render.{Oas20Renderer, RenderOptions}
import amf.client.resolve.Oas20Resolver
import amf.core.resolution.pipelines.ResolutionPipeline

import java.nio.file.{Files, Paths}
import java.util.stream.Collectors
import scala.concurrent.java8.FuturesConvertersImpl

sealed trait AccessType;
case class Public() extends AccessType
case class Private() extends AccessType

object AccessType {
  def apply(text: Option[String]) : AccessType = {
    text.map(_.toUpperCase()) match {
      case None => Public()
      case Some("PUBLIC") => Public()
      case Some("PRIVATE") => Private()
      case Some("BOTH") => Public()
      case Some("") => Public()
      case other => throw new RuntimeException(s"Unknown accessType: $other")
    }
  }
}

case class CsvApiRecord(name: String, version: String, accessType: AccessType, ramlPathOverride : Option[String])

object RepoFileExport {

  def generateOasFiles()= {
    println("**** HELLO *** ")
    csvApisToProcess.foreach(parseRaml)
  }

  def csvApisToProcess() : Seq[CsvApiRecord] = {
      val in = new FileReader("api-definition-csv-export.csv")

      org.apache.commons.csv.CSVFormat.EXCEL
        .withFirstRecordAsHeader()
        .withDelimiter(';')
        .parse(in).getRecords().asScala.toSeq
        .map(createRecord)
  }

  private def tryParseFile(csvApiRecord: CsvApiRecord, filename: String) : Try[Unit] = {
    Try({
      //val model : WebApiDocument = Raml10Parser.parse(filename).get().asInstanceOf[WebApiDocument]

      val parser = new Raml10Parser

      val model: BaseUnit = parser.parseFileAsync(filename).get



      //val webApi = model.asInstanceOf[Nothing].encodes
      val outputFilepath = s"file://generated/${csvApiRecord.name}.yaml"

     // addAccessTypeToDescription(model, csvApiRecord)

      val resolver = new Oas20Resolver
      val renderer = new Oas20Renderer

      val convertedOasApi = resolver.resolve(model, ResolutionPipeline.COMPATIBILITY_PIPELINE)
      val renderOption = RenderOptions()
      renderer.generateFile(unit = convertedOasApi, url = outputFilepath, options =  renderOption).get


    })
  }

  private def createRecord(record: CSVRecord) : CsvApiRecord = {
    def ramlPath = if (record.size() > 4) Some(record.get(4)) else None

    CsvApiRecord(
      name = record.get(0),
      version = record.get(2),
      accessType = AccessType(Option(record.get(3))),
      ramlPathOverride = ramlPath
    )
  }

//  def addAccessTypeToDescription(model : BaseUnit, api: CsvApiRecord) = {
//
//    val webApi : WebApi = model
//
//    val accessTypeDescription = api.accessType match {
//      case Public() => "This is a public API."
//      case Private() => "This is a private API."
//    }
//
//    Option(webApi.description) match {
//      case None => webApi.withDescription(accessTypeDescription)
//      case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
//      case Some(currentDescription) => webApi.withDescription(webApi.description + s" $accessTypeDescription")
//    }
//  }

  private def parseRaml(csvApiRecord: CsvApiRecord) : Unit = {

    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    val filename = s"file://api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"
    
    tryParseFile(csvApiRecord,filename) match {
      case Failure(exception) => {
        println(s"parse Raml failed: ${csvApiRecord.name}, ${csvApiRecord.version} - filename: ${filename} ${exception.toString}")
      }
      case Success(_) =>  Unit
    }
  }
}
