package openapi

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class OpenApiEnhancementsSpec extends AnyWordSpec with Matchers with OpenApiEnhancements{


    "addOasSpecAttributes " should {
        "handle empty string" in {
           addOasSpecAttributes("", "iamAnApi") shouldBe None

        }
    }
}