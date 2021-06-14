package uk.gov.hmrc.integrationcatalogueapiplatformtools.apidocsdownload


import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ListOfApisSpec extends AnyWordSpec with Matchers {

  "apis" should {
    "return a Seq of Apis" in {
      ListOfApis.apisCsvToString("agent-authorisation-api,1.0") shouldBe Seq(Api("agent-authorisation-api", "1.0"))
    }
  }
}