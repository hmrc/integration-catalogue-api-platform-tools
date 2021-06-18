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

  }

  def generateOasFiles(csvFilePath: String,  overridedRamlPath: Option[String], f : (String, String) => Unit): Future[Seq[FileExportResult]] = {
    val eventualOasResults: Future[Seq[ConvertedWebApiToOasResult]] = Future.sequence(CsvUtils.csvApisToProcess(csvFilePath)
      .map(record => {
        for {
          model <- csvRecordToRamlWebApiModelWithDescription(record, overridedRamlPath)
          convertedOasResult <- parseOasFromWebApiModel(model, record.name, record.accessType)
        } yield convertedOasResult
      }))

      processOasStrings(eventualOasResults, f)
   

  }

  def processOasStrings(eventualOasResults: Future[Seq[ConvertedWebApiToOasResult]], f : (String, String) => Unit): Future[Seq[FileExportResult]] = {
     eventualOasResults
      .map(results => {
        results.map(convertedWebApiToOasResult => {
          addOasSpecAttributes(convertedWebApiToOasResult) match {
            case Some(openApiAsString) =>f.apply(s"generated/${convertedWebApiToOasResult.apiName}.yaml", openApiAsString)
              SuccessfulFileExportResult(convertedWebApiToOasResult.apiName)
            case None                  => FailedFileExportResult(convertedWebApiToOasResult.apiName)
          }
        })
      })
  }
}