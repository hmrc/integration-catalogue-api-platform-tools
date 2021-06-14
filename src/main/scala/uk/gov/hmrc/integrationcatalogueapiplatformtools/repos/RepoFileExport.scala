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
import uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler.WebApiHandler
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import scala.concurrent.Future

object RepoFileExport extends ExtensionKeys with OpenApiEnhancements with WebApiHandler {

  def generateOasFiles(csvFilePath: String): Unit = {
    //CsvUtils.csvApisToProcess(csvFilePath).foreach(parseRaml)
    val records: Seq[Future[CsvApiRecord]] = CsvUtils.csvApisToProcess(csvFilePath).map(Future.successful(_))
   val yaparseRaml =  for{
      records <- Future.sequence(records)
      record <- records
      model <- Future.successful(getFileNameForCsvRecord(record))
      modelWithDescription <- Future.successful(model)
    } yield modelWithDescription

    // get csvrecords as seq
    // get raml for csvrecord
    // modify model with description
    // turn into OAs
    // clean up oas and add our extensions
    // write file
  }

  private def tryParseFile(csvApiRecord: CsvApiRecord, filename: String): Try[Unit] = {
 val apiName = csvApiRecord.name
 
 val outputFilepath = s"generated/${csvApiRecord.name}.yaml"
 
   val fYamlString =  for{
      model <-  parseRamlFromFileName(filename)
      yamlString <- parseOasFromWebApiModel(model)

    } yield yamlString

    fYamlString.map(yamlString => 
     addOasSpecAttributes(yamlString, apiName) match {
            case Some(openApiAsString) => writeToFile(outputFilepath, openApiAsString)
            case None                  => writeYamlFile(model, outputFilepath)
          }
    )
    Try({
      val model: WebApiDocument = parseRamlFromFileName(filename)

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

}
