/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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