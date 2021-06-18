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
       .flatMap(openapi =>  Option(openapi.getInfo()))
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
      addOasSpecAttributes(ConvertedWebApiToOasResult("", "iamAnApi", "This is a public API.")) shouldBe None

    }

    "add integration catalogue extensions to expected yaml file when short-description DOESN'T need to be truncated" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions.yaml")
      addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API.")) shouldBe Some(expectedYaml)

    }

    "add integration catalogue extensions to expected yaml file when short-description DOES need to be truncated" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-with-long-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-truncated-short-decription.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val result: Option[String] = addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."))
      result.isDefined shouldBe true
      
      val resultingShortDesc = getShortDescriptionFromOasString(result.getOrElse("")).getOrElse("")

      resultingShortDesc shouldBe expectedShortDesc
    }

    "add accessType description to expected yaml file when description does not exist" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-no-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-accessType-description.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val expectedDescription = getDescriptionFromOasString(expectedYaml).getOrElse("")
      val result: Option[String] = addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."))
      result.isDefined shouldBe true
      
      val resultingShortDesc = getShortDescriptionFromOasString(result.getOrElse("")).getOrElse("")
      val resultingDescription = getDescriptionFromOasString(result.getOrElse("")).getOrElse("")

      resultingShortDesc shouldBe expectedShortDesc
      resultingDescription shouldBe expectedDescription
    }

    "add accessType description to expected yaml file when description is empty" in new Setup {
      val contentsToParse = getFileContents("noIntCatExtensions-empty-description.yaml")
      val expectedYaml = getFileContents("expectedWithIntCatExtensions-with-accessType-description.yaml")
      val expectedShortDesc = getShortDescriptionFromOasString(expectedYaml).getOrElse("")
      val expectedDescription = getDescriptionFromOasString(expectedYaml).getOrElse("")
      val result: Option[String] = addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi", "This is a private API."))
      result.isDefined shouldBe true
      
      val resultingShortDesc = getShortDescriptionFromOasString(result.getOrElse("")).getOrElse("")
      val resultingDescription = getDescriptionFromOasString(result.getOrElse("")).getOrElse("")

      resultingDescription shouldBe expectedDescription
      resultingShortDesc shouldBe expectedShortDesc
      
    }
  }
}
