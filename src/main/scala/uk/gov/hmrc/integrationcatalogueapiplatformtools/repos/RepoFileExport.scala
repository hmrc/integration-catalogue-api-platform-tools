package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import uk.gov.hmrc.integrationcatalogueapiplatformtools.csv.CsvUtils
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi.{ExtensionKeys, OpenApiEnhancements}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler.WebApiHandler
import webapi.WebApiDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

object RepoFileExport extends ExtensionKeys with OpenApiEnhancements with WebApiHandler {

  def csvRecordToRamlWebApiModelWithDescription(csvApiRecord: CsvApiRecord, overridedRamlPath: Option[String]): Future[WebApiDocument] = {
    val filePath = overridedRamlPath.getOrElse(getFileNameForCsvRecord(csvApiRecord))
    parseRamlFromFileName(filePath)
      .map(model => {
        addAccessTypeToDescription(model, csvApiRecord)
        model
      })
  }

  def generateOasFiles(csvFilePath: String,  overridedRamlPath: Option[String]): Future[Seq[FileExportResult]] = {
    val eventualOasResults: Future[Seq[ConvertedWebApiToOasResult]] = Future.sequence(CsvUtils.csvApisToProcess(csvFilePath)
      .map(record => {

        for {
          model <- csvRecordToRamlWebApiModelWithDescription(record, overridedRamlPath)
          convertedOasResult <- parseOasFromWebApiModel(model, record.name)
        } yield convertedOasResult
      })).recover {
      case NonFatal(e) => e.printStackTrace()
        throw e
    }

    eventualOasResults
      .map(results => {
        results.map(convertedWebApiToOasResult => {
          addOasSpecAttributes(convertedWebApiToOasResult) match {
            case Some(openApiAsString) => writeToFile(s"generated/${convertedWebApiToOasResult.apiName}.yaml", openApiAsString)
              SuccessfulFileExportResult(convertedWebApiToOasResult.apiName)
            case None                  => FailedFileExportResult(convertedWebApiToOasResult.apiName)
          }
        })
      })

  }

  // $COVERAGE-OFF$
  private def writeToFile(filename: String, content: String): Unit = {
    import java.io.{BufferedWriter, File, FileWriter}

    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }
  // $COVERAGE-ON$
}
