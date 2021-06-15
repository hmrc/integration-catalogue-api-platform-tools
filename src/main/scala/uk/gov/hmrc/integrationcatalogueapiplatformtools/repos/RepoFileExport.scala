package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import amf.client.model.domain.WebApi
import uk.gov.hmrc.integrationcatalogueapiplatformtools.csv.CsvUtils
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi.{ExtensionKeys, OpenApiEnhancements}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler.WebApiHandler
import webapi.{Oas30, WebApiBaseUnit, WebApiDocument}

import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import org.slf4j.LoggerFactory
import org.apache.commons.logging.impl.Log4JLogger
import com.typesafe.scalalogging.Logger

object RepoFileExport extends ExtensionKeys with OpenApiEnhancements with WebApiHandler {


lazy val logger = Logger(LoggerFactory.getLogger("RepoFileExport"))


  def csvRecordToRamlWebApiModelWithDescription(csvApiRecord: CsvApiRecord): Future[WebApiDocument] ={
    val filePath = getFileNameForCsvRecord(csvApiRecord)
    logger.info("in csvRecordToRamlWebApiModelWithDescription")
      logger.info(filePath)
    logger.info(s"file exists ${checkpathExists(filePath)}")
    parseRamlFromFileName(filePath)
      .map(model => {logger.info(s"WebApiModel: ${model.toString}")
      addAccessTypeToDescription(model, csvApiRecord)
    model})
  }

  def checkpathExists(path: String) ={
    scala.reflect.io.File(path).exists
  }


  def printResults(results: Seq[FileExportResult]): Unit ={
    results.foreach(result => result match {
      case x: SuccessfulFileExportResult => println(s"${x.apiName} successfully processed")
      case y: FailedFileExportResult => println(s"${y.apiName} failed to process")
    })
    logger.info(s"A total of ${results.size} processed")
     logger.info(s"${results.filter(x => x.isInstanceOf[SuccessfulFileExportResult]).size} successfully processed")
     logger.info(s"${results.filter(x => x.isInstanceOf[FailedFileExportResult]).size} failed to process")
  }

  def generateOasFiles(csvFilePath: String): Future[Seq[FileExportResult]] = {

    logger.info("in generateOasFiles 1")
    val eventualOasResults: Future[Seq[ConvertedWebApiToOasResult]] = Future.sequence(CsvUtils.csvApisToProcess(csvFilePath)
      .map( record => {
       
         for{
           model <- csvRecordToRamlWebApiModelWithDescription(record)
           convertedOasResult <- parseOasFromWebApiModel(model, record.name)
         } yield convertedOasResult })
    ).recover{
      case NonFatal(e) => e.printStackTrace()
      throw e
    }

    logger.info("in generateOasFiles 2")
    eventualOasResults
      .map(results => {
        logger.info(s"${results.size}")
        results.map(convertedWebApiToOasResult => {
        // logger.info(convertedWebApiToOasResult.oasAsString) 
         logger.info("convertedWebApiToOasResult.oasAsString")
        addOasSpecAttributes(convertedWebApiToOasResult) match {
          case Some(openApiAsString) => 
                                        writeToFile( s"generated/${convertedWebApiToOasResult.apiName}.yaml", openApiAsString)
                                            logger.info("in generateOasFiles YEAH")
                                        SuccessfulFileExportResult(convertedWebApiToOasResult.apiName)
          case None                  =>      logger.info("in generateOasFiles No")
                                        FailedFileExportResult(convertedWebApiToOasResult.apiName)
        }
      })})


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

