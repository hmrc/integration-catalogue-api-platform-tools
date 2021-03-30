import org.scalatest.wordspec.AnyWordSpec

// package co.acme.translate;

import webapi.Raml10;
import webapi.Oas30;
import webapi.WebApiModule;

//import amf.client.model.domain.*;

import java.util.concurrent.ExecutionException;


class RamlSpec extends AnyWordSpec {
  // "scratch 1" in {
  //   val inp ="#%RAML 1.0\n" +
  //               "types:\n" +
  //               "  Book:\n" +
  //               "    type: object\n" +
  //               "    properties:\n" +
  //               "      title: string\n" +
  //               "      author: string\n";

  //     val x = Raml10.parse(inp).get()
  //     val fpath = "file://generated.json"
  //     Oas30.generateFile(x, fpath)
  //     System.out.println("Generating Oas30 YAML file at: " + fpath);

  //     // val doc : WebApiModule = Raml10.parse(inp).get().asInstanceOf[WebApiModule];
  // }

  // "scratch 2" in {
  //   val inp ="http://localhost:9601/api/conf/1.0/application.raml";

  //     val x = Raml10.parse(inp).get()
  //     val fpath = "file://generated/hello.json"
  //     Oas30.generateFile(x, fpath)
  //     System.out.println("Generating Oas30 YAML file at: " + fpath);

  //     // val doc : WebApiModule = Raml10.parse(inp).get().asInstanceOf[WebApiModule];
  // }

  // "Scratch 3" in {
  //   val inp ="https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/download/api-example-microservice/1.0/application.raml";

  //   val x = Raml10.parse(inp).get()
  //   val fpath = "file://generated/hello.json"
  //   Oas30.generateFile(x, fpath)
  //   System.out.println("Generating Oas30 YAML file at: " + fpath);
  //   // val doc : WebApiModule = Raml10.parse(inp).get().asInstanceOf[WebApiModule];
  // }

  "Extract API from QA API Platform" in {
    val rows = apis
      .split("\n")
      .map(row => {
        val x = row.split(",")
        (java.net.URLEncoder.encode(x(0)), x(1))
      })

    rows.foreach( row => createApiPlatformOas(row._1, row._2))
  }

  // Skipped
  /* 
  sso-frontend,1.0
  push-registration,1.0
  customer-api,1.0
  paye-des-stub,1.0
  paye-des-stub,2.0
pensions-lifetime-allowance-api,1.0
pension-scheme-administrator-lookup-api,1.0
customs-declarations,1.0

  */

  // Done
  /*

  */


  val apis = """individual-benefits,1.1
individual-employment,1.1
individual-tax,1.1
national-insurance,1.1
marriage-allowance,2.0
state-pension,1.0
api-simulator,1.0
self-assessment-api,2.0
api-documentation-test-service,0.9
api-documentation-test-service,1.0
api-documentation-test-service,1.5
api-documentation-test-service,1.6
api-documentation-test-service,2.0
api-documentation-test-service,2.1
api-documentation-test-service,2.7
api-documentation-test-service,2.10
api-documentation-test-service,2.11
api-documentation-test-service,2.12
api-documentation-test-service,2.13
api-documentation-test-service,3.0
api-documentation-test-service,4.0
api-documentation-test-service,4.1
api-documentation-test-service,4.2
api-documentation-test-service,4.3
api-documentation-test-service,4.4
api-documentation-test-service,5.0
api-documentation-test-service,6.0
trust-registration-api,1.0
national-insurance-record,1.0
lisa-api,2.0
ras-api,1.0
ras-api,2.0
individual-income,1.1
api-platform-test-user,1.0
national-insurance-des-stub,1.0
estate-registration-api,1.0
marriage-allowance-des-stub,1.0
api-example-microservice,1.0
individuals-matching-api,1.0
vat-api,1.0
customs-inventory-linking-exports,1.0
customs-inventory-linking-exports,2.0
api-notification-pull,1.0
secure-data-exchange-bulk-download,1.0
agent-authorisation-api,1.0
customs-notification-push-retry,1.0
agent-authorisation-test-support-api,1.0
customs-file-upload,1.0
individual-calculations-api,1.0
individual-losses-api,1.0
individual-losses-api,2.0
ciao-multisegment-api,2.0
ciao-multisegment-api,3.0
self-assessment-biss-api,1.0
txm-fph-validator-api,1.0
self-assessment-bsas-api,1.0
self-assessment-bsas-api,2.0
interest-restriction-return,1.0
api-platform-test,1.0
api-platform-test,2.0
api-platform-test,2.1
api-platform-test,2.2
api-platform-test,2.3
api-platform-test,3.0
api-platform-test,4.0
api-platform-test,5.0
api-platform-test,6.0
api-platform-test,7.0
goods-movement-system-haulier-api,1.0
self-assessment-accounts-api,1.0
common-transit-convention-traders,1.0
import-control-entry-declaration-store,1.0
import-control-entry-declaration-outcome,1.0
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
api-stop-autodeploy-test,1.0
api-stop-autodeploy-test,2.0
individuals-charges-api,1.0
individuals-state-benefits-api,1.0"""

  def createApiPlatformOas(serviceName: String, version: String){
    val url = s"https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/download/$serviceName/$version/application.raml";

    try{
    val x = Raml10.parse(url).get()
    val fpath = s"file://generated/qa/$serviceName-$version.json"
    Oas30.generateFile(x, fpath)
    System.out.println("Generating Oas30 YAML file at: " + fpath);
    // val doc : WebApiModule = Raml10.parse(inp).get().asInstanceOf[WebApiModule];
    } catch {
      case e => println(s"failed: $serviceName, $version - ${e.toString}")
    }
  }
}
