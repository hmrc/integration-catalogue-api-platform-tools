package uk.gov.hmrc.integrationcatalogueapiplatformtools.csv

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import org.apache.commons.csv.CSVRecord
import java.io.FileReader
import scala.collection.JavaConverters._

object CsvUtils {

  def csvApisToProcess(csvFilePath: String): Seq[CsvApiRecord] = {
    val in = new FileReader(csvFilePath)

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords.asScala
      .map(createRecord)
  }

  private def createRecord(record: CSVRecord): CsvApiRecord = {
    def ramlPath = if (record.size() > 4) Some(record.get(4)) else None

    CsvApiRecord(
      name = record.get(0),
      version = record.get(2),
      accessType = AccessType(Option(record.get(3))),
      ramlPathOverride = ramlPath
    )
  }
}
