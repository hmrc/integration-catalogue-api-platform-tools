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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.csv

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._

import scala.io.Source

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