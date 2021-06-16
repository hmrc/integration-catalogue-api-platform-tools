package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.{CsvApiRecord, Private, Public}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi.OpenApiEnhancements
import uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler.WebApiHandler
import webapi.{Raml10, WebApiDocument}

import java.util.concurrent.TimeUnit
import scala.io.Source
import scala.concurrent.{duration, Await}
import scala.concurrent.duration._
import scala.concurrent.Future
import java.nio.file.Paths
import amf.client.model.domain.WebApi

class RepoFileExportSpec extends AnyWordSpec with Matchers with MockitoSugar {

  trait Setup {

    def getWebApiDocument(filePath: String): WebApiDocument = {
      val fileContents = Source.fromResource(filePath).mkString
      Raml10.parse(fileContents)
        .get(5, TimeUnit.SECONDS).asInstanceOf[WebApiDocument]
    }

    def absoluteFilePath = "file://" + Paths.get(".").toAbsolutePath().toString().replace(".", "") + "src/test/resources/test-ramlFile-with-description.raml"
    val csvApiRecordPublicAccess = CsvApiRecord("public-apiname", "1.0", Public(), None)
    val csvApiRecordPrivateAccess = CsvApiRecord("private-apiname", "1.0", Private(), None)

    def webApiDocumentWithDescription = getWebApiDocument("test-ramlFile-with-description.raml")
  }

  "csvRecordToRamlWebApiModelWithDescription" should {
    "return webapidocument when filepath is a raml file" in new Setup {
      val expectedDescription = "A description. This is a public API."
      val result = Await.result(RepoFileExport.csvRecordToRamlWebApiModelWithDescription(csvApiRecordPublicAccess, Some(absoluteFilePath)), 10 seconds)

      result.encodes.asInstanceOf[WebApi].description.value shouldBe expectedDescription

    }
  }

}
