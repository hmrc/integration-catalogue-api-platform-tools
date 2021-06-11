package openapi

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.io.Source


class OpenApiEnhancementsSpec extends AnyWordSpec with Matchers with OpenApiEnhancements{

  trait Setup {
    def getFileContents(filePath: String) : String = {
      Source.fromResource(filePath).mkString
    }
    
  }


    "addOasSpecAttributes " should {
        "handle empty string" in {
           addOasSpecAttributes("", "iamAnApi") shouldBe None

        }

        "add integration catalogue extensions to expected yaml file" in new Setup {
          val contentsToParse = getFileContents("noIntCatExtensions.yaml")
          val expectedYaml = getFileContents("expectedWithIntCatExtensions.yaml")
           addOasSpecAttributes(contentsToParse, "iamAnApi") shouldBe Some(expectedYaml)

        }
    }
}