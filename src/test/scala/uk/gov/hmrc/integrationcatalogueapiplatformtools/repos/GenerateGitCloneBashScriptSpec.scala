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