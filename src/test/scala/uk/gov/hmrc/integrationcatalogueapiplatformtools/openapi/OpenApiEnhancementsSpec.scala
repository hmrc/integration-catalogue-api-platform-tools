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