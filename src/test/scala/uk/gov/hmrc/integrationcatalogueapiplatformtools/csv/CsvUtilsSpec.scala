package uk.gov.hmrc.integrationcatalogueapiplatformtools.csv

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CsvUtilsSpec  extends AnyWordSpec with Matchers {


  // "/Users/anjumabbas/IdeaProjects/integration-catalogue-api-platform-tools/src/test/resources/test-api-definition-csv-export.csv"

  "csvApisToProcess" should {
    "return a Seq of 2 CsvApiRecord's when given a csv with 2 entries" in {
      CsvUtils.csvApisToProcess("resources/test-api-definition-csv-export.csv")

    }
  }

}