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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._


class GenerateGitCloneBashScriptSpec extends AnyWordSpec with Matchers {

  "printScript" should {
    "return 2 git clone lines" in {
       val csvRecords: Seq[CsvApiRecord] = Seq(CsvApiRecord("api1", "1.0.0", Public(), None), CsvApiRecord("api2", "1.1.0", Public(), None))
      GenerateGitCloneBashScript.printScript(csvRecords) shouldBe "git clone git@github.com:hmrc/api1.git\ngit clone git@github.com:hmrc/api2.git"
    }

  }
}