import org.scalatest.wordspec.AnyWordSpec

// package co.acme.translate;

import webapi.Raml10;
import webapi.Oas30;
import webapi.WebApiModule;

//import amf.client.model.domain.*;

import java.util.concurrent.ExecutionException;
import webapi.WebApiBaseUnit
import javax.xml.transform.Source
import java.io.BufferedWriter
import java.io.FileWriter


class RamlSpec extends AnyWordSpec {
  "Extract API from QA API Platform" in {
    val rows = apis
      .split("\n")
      .map(row => {
        val rowItems = row.split(",")
        (java.net.URLEncoder.encode(rowItems(0)), rowItems(1))
      })

    rows
      .foreach( row => createApiPlatformOas(row._1, row._2))
  }

  val apis1 = """trust-registration-api,1.0"""

  val apis = """trust-registration-api,1.0
national-insurance-record,1.0
lisa-api,2.0
ras-api,1.0
ras-api,2.0
individual-income,1.2
national-insurance-des-stub,1.0
estate-registration-api,1.0
marriage-allowance-des-stub,1.0
individuals-matching-api,1.0
vat-api,1.0
customs-inventory-linking-exports,2.0
api-notification-pull,1.0
customs-declarations,1.0
secure-data-exchange-bulk-download,1.0
agent-authorisation-api,1.0
customs-notification-push-retry,1.0
agent-authorisation-test-support-api,1.0
customs-file-upload,1.0
individual-calculations-api,1.0
individual-losses-api,2.0
ciao-multisegment-api,3.0
self-assessment-biss-api,1.0
txm-fph-validator-api,1.0
self-assessment-bsas-api,2.0
interest-restriction-return,1.0
goods-movement-system-haulier-api,1.0
self-assessment-accounts-api,1.0
common-transit-convention-traders,1.0
import-control-entry-declaration-store,1.0
import-control-entry-declaration-intervention,1.0
business-details-api,1.0
individuals-reliefs-api,1.0
obligations-api,1.0
individuals-income-received-api,1.0
push-pull-notifications-api,1.0
cis-deductions-api,1.0
other-deductions-api,1.0
property-business-api,1.0
common-transit-convention-traders-test-support,1.0
individuals-charges-api,1.0
individuals-state-benefits-api,1.0
check-eori-number-api,1.0
vat-registered-companies-api,1.0
api-open-access-test-service,1.0"""

  def createApiPlatformOas(serviceName: String, version: String){
    val url = s"https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/download/$serviceName/$version/application.raml";

    try{
      val model : WebApiBaseUnit = Raml10.parse(url).get()

      val outputFilepath = s"file://generated/qa/$serviceName-$version.yaml"
       Oas30.generateYamlFile(model, outputFilepath)
      // Oas30.generateFile(model, outputFilepath)

      println("Generated Oas30 YAML file at: " + outputFilepath);      
    } catch {
      case e : Throwable => println(s"failed: $serviceName, $version - ${e.toString}")
    }
  }
}
