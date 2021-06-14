package uk.gov.hmrc.integrationcatalogueapiplatformtools.csv

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, InputStream, InputStreamReader}
import scala.io.Source

class CsvUtilsSpec  extends AnyWordSpec with Matchers {


  // "/Users/anjumabbas/IdeaProjects/integration-catalogue-api-platform-tools/src/test/resources/test-api-definition-csv-export.csv"

  "csvApisToProcess" should {
    "return a Seq of 2 CsvApiRecord's when given a csv with 2 entries" in {
      CsvUtils.csvApisToProcessWithReader( Source.fromResource("test-api-definition-csv-export.csv").bufferedReader())

    }
  }

}