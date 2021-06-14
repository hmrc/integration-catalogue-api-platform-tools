package uk.gov.hmrc.integrationcatalogueapiplatformtools.csv

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, InputStream, InputStreamReader}
import scala.io.Source

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._

class CsvUtilsSpec  extends AnyWordSpec with Matchers {

  "csvApisToProcess" should {
    "return a Seq of 4 CsvApiRecord's when given a csv with 5 entries" in {

      val expected = Seq(
        CsvApiRecord("address-lookup",  "1.0", Private(), Some("public/api/conf")),
        CsvApiRecord("agent-authorisation-api", "1.0", Public(), None),
        CsvApiRecord( "agent-authorisation-test-support-api", "1.0", Public(), None),
        CsvApiRecord( "agent-authorisation-test-support-api", "1.0", Public(), None)
        )
      val results: Seq[CsvApiRecord] = CsvUtils.csvApisToProcessWithReader( Source.fromResource("test-api-definition-csv-export.csv").bufferedReader())
      results.size shouldBe 4
      results.toList shouldBe expected.toList
    }
  }

}