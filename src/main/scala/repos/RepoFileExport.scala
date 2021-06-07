package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import amf.client.model.domain.WebApi
import openapi.{ExtensionKeys, OpenApiEnhancements}
import org.apache.commons.csv.CSVRecord
import webapi.{Oas30, Raml10, WebApiBaseUnit, WebApiDocument}

import java.io.FileReader
import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

sealed trait AccessType;

case class Public() extends AccessType

case class Private() extends AccessType

object AccessType {
  def apply(text: Option[String]): AccessType = {
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

case class CsvApiRecord(name: String, version: String, accessType: AccessType, ramlPathOverride: Option[String])

object RepoFileExport extends ExtensionKeys with OpenApiEnhancements {

  def generateOasFiles() = {
    csvApisToProcess.foreach(parseRaml)
  }

  def csvApisToProcess(): Seq[CsvApiRecord] = {
    val in = new FileReader("api-definition-csv-export.csv")

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords().asScala.toSeq
      .map(createRecord)
  }




  private def tryParseFile(csvApiRecord: CsvApiRecord, filename: String): Try[Unit] = {
    Try({
      val model: WebApiDocument = Raml10.parse(filename).get().asInstanceOf[WebApiDocument]

      val apiName = csvApiRecord.name

      val outputFilepath = s"generated/${csvApiRecord.name}.yaml"

      addAccessTypeToDescription(model, csvApiRecord)
      val yamlFileAsString: CompletableFuture[String] = Oas30.generateYamlString(model)

      addOasSpecAttributes(yamlFileAsString, apiName) match {
        case Some(openApiAsString) => {
          writeToFile(outputFilepath, openApiAsString)
        }
        case None => { // fallback case
          writeYamlFile(model, outputFilepath)
        }
      }
    })
  }

  private def writeYamlFile(model: WebApiBaseUnit, outputFilepath: String) = {
    val f: CompletableFuture[Unit] = Oas30.generateYamlFile(model, outputFilepath)
    f.get(60, TimeUnit.SECONDS)
  }

  private def writeToFile(filename: String, content: String): Unit = {
    import java.io.{BufferedWriter, File, FileWriter}

    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

  private def createRecord(record: CSVRecord): CsvApiRecord = {
    def ramlPath = if (record.size() > 4) Some(record.get(4)) else None

    CsvApiRecord(
      name = record.get(0),
      version = record.get(2),
      accessType = AccessType(Option(record.get(3))),
      ramlPathOverride = ramlPath
    )
  }

  private def addAccessTypeToDescription(model: WebApiDocument, api: CsvApiRecord) = {

    val webApi: WebApi = model.encodes.asInstanceOf[WebApi]

    val accessTypeDescription = api.accessType match {
      case Public() => "This is a public API."
      case Private() => "This is a private API."
    }

    Option(webApi.description) match {
      case None => webApi.withDescription(accessTypeDescription)
      case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
      case Some(currentDescription) => webApi.withDescription(webApi.description + s" $accessTypeDescription")
    }
  }

  private def parseRaml(csvApiRecord: CsvApiRecord): Unit = {
    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    val filename = s"file://api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"

    tryParseFile(csvApiRecord, filename) match {
      case Failure(exception) => {
        println(s"failed: ${csvApiRecord.name}, ${csvApiRecord.version} - filename: ${filename} ${exception.toString}")
      }
      case Success(value) => Unit
    }
  }
}
