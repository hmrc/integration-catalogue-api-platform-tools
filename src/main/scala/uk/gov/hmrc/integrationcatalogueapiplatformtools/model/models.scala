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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.model

sealed trait AccessType

case class Public() extends AccessType

case class Private() extends AccessType

object AccessType {

  def apply(text: Option[String]): AccessType = {
    text.map(_.toUpperCase()) match {
      case None            => Public()
      case Some("PUBLIC")  => Public()
      case Some("PRIVATE") => Private()
      case Some("BOTH")    => Public()
      case Some("")        => Public()
      case other           => throw new RuntimeException(s"Unknown accessType: $other")
    }
  }
}

case class CsvApiRecord(name: String, version: String, accessType: AccessType, ramlPathOverride: Option[String])

case class ConvertedWebApiToOasResult(oasAsString: String, apiName: String, accessTypeDescription: String)


sealed trait FileExportResult
case class SuccessfulFileExportResult(apiName: String)  extends FileExportResult
case class FailedFileExportResult(apiName: String, message: String)  extends FileExportResult


sealed trait OpenApiProcessingError

case class GeneralOpenApiProcessingError(apiName: String, message: String) extends OpenApiProcessingError