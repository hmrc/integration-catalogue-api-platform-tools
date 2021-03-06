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

import amf.client.model.domain.WebApi
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import webapi.{Raml10, WebApiDocument}

import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.Source

class RepoFileExportSpec extends AnyWordSpec with Matchers with MockitoSugar {

  trait Setup {

    val validISODate = "2021-12-25T12:00:00Z"

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def mockWriteToFile(val1: String, val2: String): Unit = {
      ()
    }

    def absoluteRamlFilePath = "file://" + Paths.get(".").toAbsolutePath().toString().replace(".", "") + "src/test/resources/test-ramlFile-with-description.raml"
    val csvApiRecordPublicAccess = CsvApiRecord("public-apiname", "1.0", Public(), None)
    val csvApiRecordPrivateAccess = CsvApiRecord("private-apiname", "1.0", Private(), None)

    def webApiDocumentWithDescription = getWebApiDocument("test-ramlFile-with-description.raml")
  }

  "csvRecordToRamlWebApiModelWithDescription" should {
   


    "return webapidocument when filepath is a raml file" in new Setup {
      val expectedDescription = "A description."
      val result = Await.result(RepoFileExport.csvRecordToRamlWebApiModelWithDescription(csvApiRecordPublicAccess, Some(absoluteRamlFilePath)), 10 seconds)

      result.encodes.asInstanceOf[WebApi].description.value shouldBe expectedDescription

    }
  }

  "processOasStrings" should {

   
    "return SuccessfulFileExportResult" in new Setup {
      val apiName = "Api Name"
      val oasString = Source.fromResource("noIntCatExtensions.yaml").mkString
      val oasStrings = Future.successful(Seq(ConvertedWebApiToOasResult(oasString, apiName, "This is a private API.")))
      val result = Await.result(RepoFileExport.processOasStrings(oasStrings, validISODate, mockWriteToFile), 10 seconds)
      result shouldBe Seq(SuccessfulFileExportResult(apiName))
    }

    "return FailedFileExportResult" in new Setup {
      val apiName = "Api Name"
      val oasString = ""
      val oasStrings = Future.successful(Seq(ConvertedWebApiToOasResult(oasString, apiName, "This is a private API.")))
      val result = Await.result(RepoFileExport.processOasStrings(oasStrings, validISODate, mockWriteToFile), 10 seconds)
      result shouldBe Seq(FailedFileExportResult(apiName, "Swagger Parse failure"))
    }
  }

  "generateOasFiles" should {
    "return SuccessfulFileExportResult" in new Setup {
      def absoluteCsvFilePath = Paths.get(".").toAbsolutePath().toString().replace(".", "") + "src/test/resources/test-api-definition-csv-export-with-single-record.csv"
      val result = Await.result(RepoFileExport.generateOasFiles(absoluteCsvFilePath, Some(absoluteRamlFilePath), mockWriteToFile, validISODate), 10 seconds)
      result shouldBe Seq(SuccessfulFileExportResult("address-lookup"))
    }

    "return FailedFileExportResult when invalid reviewed date" in new Setup {
      def absoluteCsvFilePath = Paths.get(".").toAbsolutePath().toString().replace(".", "") + "src/test/resources/test-api-definition-csv-export-with-single-record.csv"
      val result = Await.result(RepoFileExport.generateOasFiles(absoluteCsvFilePath, Some(absoluteRamlFilePath), mockWriteToFile, "2021-89-25T12:00:00Z"), 10 seconds)
      result shouldBe Seq(FailedFileExportResult("", "Reviewed date is not a valid ISO 8601 date"))
    }
  }

}
