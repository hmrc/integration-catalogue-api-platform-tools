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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.ConvertedWebApiToOasResult
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.{ParseOptions, SwaggerParseResult}

import scala.io.Source
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.OpenApiProcessingError
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.GeneralOpenApiProcessingError

class OpenApiEnhancementsSpec extends AnyWordSpec with Matchers with OpenApiEnhancements {

  trait Setup {

    def getFileContents(filePath: String): String = {
      Source.fromResource(filePath).mkString
    }

    def getInfo(yaml: String) = {
      val options: ParseOptions = new ParseOptions()
      options.setResolve(false)
      Option(new OpenAPIV3Parser().readContents(yaml, null, options))
        .flatMap(swaggerResult => Option(swaggerResult.getOpenAPI()))
        .flatMap(openapi => Option(openapi.getInfo()))
    }

    def getShortDescriptionFromOasString(yaml: String): Option[String] = {
      getInfo(yaml)
        .flatMap(info => Option(info.getExtensions()))
        .flatMap(extensions => Option(extensions.get(EXTENSIONS_KEY).asInstanceOf[java.util.Map[String, Any]]))
        .map(subLevelExtensions => subLevelExtensions.get(SHORT_DESC_EXTENSION_KEY).toString)
    }

    def getDescriptionFromOasString(yaml: String): Option[String] = {
      getInfo(yaml)
        .flatMap(info => Option(info.getDescription()))
    }

  }

  "addOasSpecAttributes " should {
    "handle empty string" in {
      addOasSpecAttributes(ConvertedWebApiToOasResult("", "iamAnApi", "This is a public API.")) shouldBe Left(GeneralOpenApiProcessingError("iamAnApi", "Swagger Parse failure"))

    }

    "add integration catalogue extensions to expected yaml file when short-description DOESN'T need to be truncated" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions.yaml")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) match {
        case Left(_)          => fail()
        case Right(oasString) => oasString shouldBe expectedYaml
      }

    }

    "Return Left(GeneralOpenApiProcessingError) if contents to parse has invalid x-amf-userDocumentation content" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-withInvalidXAmfDocumentation.yaml")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) match {
        case Right(_)                                   => fail()
        case Left(error: GeneralOpenApiProcessingError) => {
          error.apiName shouldBe "iamAnApi"
          error.message shouldBe "API: iamAnApi content for title: Versioning points to a file!! https://developer.service.hmrc.gov.uk/api-documentation/assets/common/docs/versioning.md \nAPI: iamAnApi content for title: Errors points to a file!! https://developer.service.hmrc.gov.uk/api-documentation/assets/common/docs/errors.md \n"
        }
      }

    }
    "Return Right if contents to parse does not have x-amf-userDocumentation tag" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-withNOxAmfDocumentation.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-NOxAmfDocumentation.yaml")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) match {
        case Left(_)          => fail()
        case Right(oasString) => oasString shouldBe expectedYaml
      }

    }

    "add integration catalogue extensions to expected yaml file when short-description DOES need to be truncated" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-with-long-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-truncated-short-decription.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          val resultingShortDesc = getShortDescriptionFromOasString(oasString).getOrElse("")

          resultingShortDesc shouldBe expectedShortDesc
        }
      }
    }

    "add accessType description to expected yaml file when description does not exist" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-no-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-accessType-description.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val expectedDescription = getDescriptionFromOasString(expectedYaml).getOrElse("")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          val resultingShortDesc = getShortDescriptionFromOasString(oasString).getOrElse("")
          val resultingDescription = getDescriptionFromOasString(oasString).getOrElse("")

          resultingShortDesc shouldBe expectedShortDesc
          resultingDescription shouldBe expectedDescription
        }
      }

    }

    "add accessType description to expected yaml file when description is empty" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-empty-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-accessType-description.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val expectedDescription = getDescriptionFromOasString(expectedYaml).getOrElse("")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) match {
        case Left(_)          => fail()
        case Right(oasString) => {
          val resultingShortDesc = getShortDescriptionFromOasString(oasString).getOrElse("")
          val resultingDescription = getDescriptionFromOasString(oasString).getOrElse("")

          resultingDescription shouldBe expectedDescription
          resultingShortDesc shouldBe expectedShortDesc
        }
      }

    }
  }
}
