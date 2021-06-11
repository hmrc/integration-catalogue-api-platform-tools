package apidocsdownload

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.integrationcatalogueapiplatformtools.apidocsdownload.ListOfApis
import uk.gov.hmrc.integrationcatalogueapiplatformtools.apidocsdownload.Api

class ListOfApisSpec extends AnyWordSpec with Matchers {

  "apis" should {
    "return a Seq of Apis" in {
      ListOfApis.apisCsvToString("agent-authorisation-api,1.0") shouldBe Seq(Api("agent-authorisation-api", "1.0"))
    }
  }
}