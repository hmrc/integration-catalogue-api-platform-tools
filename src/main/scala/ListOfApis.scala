case class Api(serviceName: String, version: String)

object ListOfApis {
    val apisCsv = """agent-authorisation-api,1.0
business-details-api,1.0
check-eori-number-api,1.0
cis-deductions-api,1.0
common-transit-convention-traders,1.0
customs-declarations,1.0
customs-file-upload,1.0
customs-inventory-linking-exports,2.0
customs-notification-push-retry,1.0
estate-registration-api,1.0
goods-movement-system-haulier-api,1.0
import-control-entry-declaration-intervention,1.0
import-control-entry-declaration-store,1.0
individual-calculations-api,1.0
individual-income,1.2
individual-losses-api,2.0
individuals-charges-api,1.0
individuals-income-received-api,1.0
individuals-matching-api,1.0
individuals-reliefs-api,1.0
individuals-state-benefits-api,1.0
lisa-api,2.0
marriage-allowance-des-stub,1.0
national-insurance-des-stub,1.0
national-insurance-record,1.0
obligations-api,1.0
other-deductions-api,1.0
property-business-api,1.0
ras-api,1.0
ras-api,2.0
secure-data-exchange-bulk-download,1.0
self-assessment-accounts-api,1.0
self-assessment-biss-api,1.0
self-assessment-bsas-api,2.0
trust-registration-api,1.0
vat-api,1.0
vat-registered-companies-api,1.0"""

// Skipped
/*
agent-authorisation-test-support-api,1.0
api-notification-pull,1.0
api-open-access-test-service,1.0
common-transit-convention-traders-test-support,1.0
push-pull-notifications-api,1.0
txm-fph-validator-api,1.0
ciao-multisegment-api,3.0       // TODO: Not sure this is a real API
interest-restriction-return,1.0 // TODO: Generated lots of 404 trying to resolve definitions.json
*/

  def apis() : Seq[Api] = {
    apisCsv
      .split("\n")
      .map(row => {
        val rowItems = row.split(",") 
        //(java.net.URLEncoder.encode(rowItems(0)), rowItems(1))
        Api(rowItems(0), rowItems(1))
      })
  }
}
