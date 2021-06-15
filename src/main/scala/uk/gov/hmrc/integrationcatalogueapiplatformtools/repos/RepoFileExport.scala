package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import amf.client.model.domain.WebApi
import uk.gov.hmrc.integrationcatalogueapiplatformtools.csv.CsvUtils
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi.{ExtensionKeys, OpenApiEnhancements}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler.{ConvertedWebApiToOasResult, WebApiHandler}
import webapi.{Oas30, WebApiBaseUnit, WebApiDocument}

import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object RepoFileExport extends ExtensionKeys with OpenApiEnhancements with WebApiHandler {


  def csvRecordToRamlWebApiModelWithDescription(csvApiRecord: CsvApiRecord): Future[WebApi] ={
    parseRamlFromFileName(getFileNameForCsvRecord(csvApiRecord))
      .map(model => addAccessTypeToDescription(model, csvApiRecord))
  }


  def generateOasFiles(csvFilePath: String) = {

    val eventualOasResults: Future[Seq[ConvertedWebApiToOasResult]] = Future.sequence(CsvUtils.csvApisToProcess(csvFilePath)
      .map( record => {
         for{
           model <- csvRecordToRamlWebApiModelWithDescription(record)
           convertedOasResult <- parseOasFromWebApiModel(model.asInstanceOf[WebApiDocument], record.name)
         } yield convertedOasResult })
    )

    eventualOasResults
      .map(results => results.map(convertedWebApiToOasResult => {
        addOasSpecAttributes(convertedWebApiToOasResult) match {
          case Some(openApiAsString) => Future.successful(writeToFile( s"generated/$openApiAsString.yaml", openApiAsString))
          case None                  => Future.failed(new RuntimeException("unable to process record"))
        }
      }))


  }







    // get csvrecords as seq
    // get raml for csvrecord
    // modify model with description
    // turn into OAs
    // clean up oas and add our extensions
    // write file

//
//  private def tryParseFile(csvApiRecord: CsvApiRecord, filename: String): Try[Unit] = {
// val apiName = csvApiRecord.name
//
//
//
//   val fYamlString =  for{
//      model <-  parseRamlFromFileName(filename)
//      yamlString <- parseOasFromWebApiModel(model)
//
//    } yield yamlString
//
//    fYamlString.map(yamlString =>
//     addOasSpecAttributes(yamlString, apiName) match {
//            case Some(openApiAsString) => writeToFile(outputFilepath, openApiAsString)
//            case None                  => writeYamlFile(model, outputFilepath)
//          }
//    )
//    Try({
//      val model: WebApiDocument = parseRamlFromFileName(filename)
//
//      addAccessTypeToDescription(model, csvApiRecord)
//
//      FutureConverters.toScala(Oas30.generateYamlString(model))
//        .map(yamlString => {
//          addOasSpecAttributes(yamlString, apiName) match {
//            case Some(openApiAsString) => writeToFile(outputFilepath, openApiAsString)
//            case None                  => writeYamlFile(model, outputFilepath)
//          }
//        })
//
//    })
//  }

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
