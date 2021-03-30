case class Api(serviceName: String, version: String)



object ListOfApis {
    val apisCsv = """
agent-authorisation-api,1.0
agent-authorisation-test-support-api,1.0
secure-data-exchange-bulk-download,1.0
business-details-api,1.0
self-assessment-biss-api,1.0
self-assessment-bsas-api,2.0
vat-registered-companies-api,1.0
check-eori-number-api,1.0
cis-deductions-api,1.0
common-transit-convention-traders,1.0
api-platform-test-user,1.0
customs-inventory-linking-exports,1.0
goods-movement-system-haulier-api,1.0
api-example-microservice,1.0
individual-benefits,1.1
individual-calculations-api,2.0
individual-employment,1.2
individual-income,1.2
individual-losses-api,2.0
paye-des-stub,1.0
individual-tax,1.1
individuals-business-eops-api,1.0
individuals-charges-api,1.0
individuals-disclosures-api,1.0
individuals-expenses-api,1.0
individuals-income-received-api,1.0
individuals-reliefs-api,1.0
individuals-state-benefits-api,1.0
interest-restriction-return,1.0
lisa-api,2.0
marriage-allowance,2.0
marriage-allowance-des-stub,1.0
national-insurance,1.1
national-insurance-des-stub,1.0
obligations-api,1.0
other-deductions-api,1.0
property-business-api,1.0
api-notification-pull,1.0
push-pull-notifications-api,1.0
ras-api,2.0
// import-control-entry-declaration-store,1.0
// import-control-entry-declaration-intervention,1.0
// import-control-entry-declaration-outcome,1.0
self-assessment-api,2.0
self-assessment-accounts-api,1.0
txm-fph-validator-api,1.0
vat-api,1.0
"""

  def apis() : Seq[Api] = {
    apisCsv
      .split("\n")
      .filter(row => row.trim.nonEmpty)
      .filter(row => !row.startsWith("//"))
      .map(row => {
        val rowItems = row.split(",")
        //(java.net.URLEncoder.encode(rowItems(0)), rowItems(1))
        Api(rowItems(0), rowItems(1))
      })
  }
}
