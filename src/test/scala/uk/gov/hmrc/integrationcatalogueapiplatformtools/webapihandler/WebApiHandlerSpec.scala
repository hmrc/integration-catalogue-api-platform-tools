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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler

import amf.client.model.domain.WebApi
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import webapi.{Raml10, WebApiDocument}

import java.util.concurrent.TimeUnit
import scala.io.Source



class WebApiHandlerSpec extends AnyWordSpec with Matchers with WebApiHandler {

  trait Setup {
    def getWebApiDocument(filePath: String) : WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    val csvApiRecordPublicAccess = CsvApiRecord("public-apiname", "1.0", Public(), None)
    val csvApiRecordPrivateAccess = CsvApiRecord("private-apiname", "1.0", Private(), None)
    def webApiDocumentWithDescription = getWebApiDocument("test-ramlFile-with-description.raml")
    def webApiDocumentWithEmptyDescription = getWebApiDocument("test-ramlFile-with-empty-description.raml")
    def webApiDocumentWithoutDescription = getWebApiDocument("test-ramlFile-without-description.raml")
  }

  "getFileNameForCsvRecord" should {

    "should return file path with default resources path when raml overide is not set" in {
      val csvApiRecord = CsvApiRecord("apiname", "1.0", Public(), None)
      val expectedFilePath = s"file://api-repos/${csvApiRecord.name}/resources/public/api/conf/${csvApiRecord.version}/application.raml"

      getFileNameForCsvRecord(csvApiRecord) shouldBe expectedFilePath
    }

    "should return file path with overridden raml path when raml overide is set" in {
        val overriddenPath = "public/api/documentation"
      val csvApiRecord = CsvApiRecord("apiname", "1.0", Public(), Some(overriddenPath))
      val expectedFilePath = s"file://api-repos/${csvApiRecord.name}/$overriddenPath/${csvApiRecord.version}/application.raml"

      getFileNameForCsvRecord(csvApiRecord) shouldBe expectedFilePath
    }
  }

  "addAccessTypeToDescription" should {

    "return webapi with public description when csvapirecord has public access and webapidocument has no description" in new Setup {

      val expectedWebApi: WebApi = webApiDocumentWithoutDescription.encodes.asInstanceOf[WebApi]
      expectedWebApi.withDescription("This is a public API.")

      val resultingWebApi: WebApi = addAccessTypeToDescription(webApiDocumentWithoutDescription, csvApiRecordPublicAccess)
      resultingWebApi.description.value shouldBe expectedWebApi.description.value
    }

    "return webapi with private description when csvapirecord has private access and webapidocument has no description" in new Setup {

      val expectedWebApi: WebApi = webApiDocumentWithoutDescription.encodes.asInstanceOf[WebApi]
      expectedWebApi.withDescription("This is a private API.")
      val resultingWebApi: WebApi = addAccessTypeToDescription(webApiDocumentWithoutDescription, csvApiRecordPrivateAccess)
      resultingWebApi.description.value shouldBe expectedWebApi.description.value
    }

    "return webapi with private description when csvapirecord has private access and webapidocument has an empty description" in new Setup {

      val expectedWebApi: WebApi =       webApiDocumentWithEmptyDescription.encodes.asInstanceOf[WebApi]
      expectedWebApi.withDescription("This is a private API.")
      val resultingWebApi: WebApi = addAccessTypeToDescription(webApiDocumentWithEmptyDescription, csvApiRecordPrivateAccess)
      resultingWebApi.description.value shouldBe expectedWebApi.description.value
    }

    "return webapi with private description when csvapirecord has private access and webapidocument has a description" in new Setup {

      val expectedWebApi: WebApi =       webApiDocumentWithDescription.encodes.asInstanceOf[WebApi]
      expectedWebApi.withDescription("A description. This is a private API.")
      val resultingWebApi: WebApi = addAccessTypeToDescription(webApiDocumentWithDescription, csvApiRecordPrivateAccess)
      resultingWebApi.description.value shouldBe expectedWebApi.description.value
    }
  }

}
