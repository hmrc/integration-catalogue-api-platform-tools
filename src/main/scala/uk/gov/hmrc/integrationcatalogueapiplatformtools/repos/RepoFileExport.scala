package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import amf.client.model.domain.WebApi
import org.apache.commons.csv.CSVRecord
import uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi.{ExtensionKeys, OpenApiEnhancements}
import webapi.{Oas30, Raml10, WebApiBaseUnit, WebApiDocument}

import java.io.FileReader
import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import scala.compat.java8._
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.integrationcatalogueapiplatformtools.csv.CsvUtils
import uk.gov.hmrc.integrationcatalogueapiplatformtools
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._

object RepoFileExport extends ExtensionKeys with OpenApiEnhancements {

  def generateOasFiles(csvFilePath: String): Unit = {
    CsvUtils.csvApisToProcess(csvFilePath).foreach(parseRaml)
  }

  private def tryParseFile(csvApiRecord: CsvApiRecord, filename: String): Try[Unit] = {
    Try({
      val model: WebApiDocument = Raml10.parse(filename).get().asInstanceOf[WebApiDocument]

      val apiName = csvApiRecord.name

      val outputFilepath = s"generated/${csvApiRecord.name}.yaml"

      addAccessTypeToDescription(model, csvApiRecord)

      FutureConverters.toScala(Oas30.generateYamlString(model))
        .map(yamlString => {
          addOasSpecAttributes(yamlString, apiName) match {
            case Some(openApiAsString) => writeToFile(outputFilepath, openApiAsString)
            case None                  => writeYamlFile(model, outputFilepath)
          }
        })

    })
  }

  private def writeYamlFile(model: WebApiBaseUnit, outputFilepath: String): Unit = {
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

  private def addAccessTypeToDescription(model: WebApiDocument, api: CsvApiRecord) = {

    val webApi: WebApi = model.encodes.asInstanceOf[WebApi]

    val accessTypeDescription = api.accessType match {
      case Public()  => "This is a public API."
      case Private() => "This is a private API."
    }

    Option(webApi.description) match {
      case None                       => webApi.withDescription(accessTypeDescription)
      case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
      case Some(_)   => webApi.withDescription(webApi.description + s" $accessTypeDescription")
    }
  }

  private def parseRaml(csvApiRecord: CsvApiRecord): Unit = {
    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    val filename = s"file://api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"

    tryParseFile(csvApiRecord, filename) match {
      case Failure(exception) =>
        println(s"failed: ${csvApiRecord.name}, ${csvApiRecord.version} - filename: $filename ${exception.toString}")
      case Success(_)     => Unit
    }
  }
}
