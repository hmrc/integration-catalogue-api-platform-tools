package uk.gov.hmrc.integrationcatalogueapiplatformtools


import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos._
import uk.gov.hmrc.integrationcatalogueapiplatformtools.csv.CsvUtils
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.{FailedFileExportResult, FileExportResult, SuccessfulFileExportResult}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}
// $COVERAGE-OFF$
object Main extends App with FileWriterUtils {

  lazy val logger = Logger(LoggerFactory.getLogger("mainLogger"))

  def printResults(results: Seq[FileExportResult]): Unit ={
    results.foreach(result => result match {
      case x: SuccessfulFileExportResult =>  ()
      case y: FailedFileExportResult =>  logger.info(s"${y.apiName} failed to process")
    })
    logger.info(s"A total of ${results.size} processed")
    logger.info(s"${results.filter(x => x.isInstanceOf[SuccessfulFileExportResult]).size} successfully processed")
    logger.info(s"${results.filter(x => x.isInstanceOf[FailedFileExportResult]).size} failed to process")
  }


  args.toList match {
    case "--help" :: Nil => println("Print usage instructions")
    case "--generateGitClone" :: Nil =>
      val csvRecords = CsvUtils.csvApisToProcess("api-definition-csv-export.csv")
      val linesForBashScript = GenerateGitCloneBashScript.printScript(csvRecords)
      println(linesForBashScript)
    case "--generateOas" :: Nil => {
      val result = RepoFileExport.generateOasFiles("api-definition-csv-export.csv", None, writeToFile)
      result.onComplete {
        case Success(value) => printResults(value)
        case Failure(e) => logger.error("failed!!", e)
      }
      Await.result(result, 30 seconds)
    }
    case unknown => println(s"Unrecognised arguments: $unknown")
  }
}

// $COVERAGE-ON$