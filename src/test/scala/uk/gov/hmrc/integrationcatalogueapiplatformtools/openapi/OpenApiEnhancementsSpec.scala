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

import scala.io.Source


class OpenApiEnhancementsSpec extends AnyWordSpec with Matchers with OpenApiEnhancements{

  trait Setup {
    def getFileContents(filePath: String) : String = {
      Source.fromResource(filePath).mkString
    }
    
  }


    "addOasSpecAttributes " should {
        "handle empty string" in {
           addOasSpecAttributes(ConvertedWebApiToOasResult("", "iamAnApi")) shouldBe None

        }

        "add integration catalogue extensions to expected yaml file" in new Setup {
          val contentsToParse = getFileContents("noIntCatExtensions.yaml")
          val expectedYaml = getFileContents("expectedWithIntCatExtensions.yaml")
           addOasSpecAttributes(ConvertedWebApiToOasResult(contentsToParse, "iamAnApi")) shouldBe Some(expectedYaml)

        }
    }
}