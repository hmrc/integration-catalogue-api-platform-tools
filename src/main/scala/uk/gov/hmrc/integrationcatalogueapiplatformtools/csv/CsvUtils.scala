package uk.gov.hmrc.integrationcatalogueapiplatformtools.csv

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import org.apache.commons.csv.CSVRecord

import java.io.{FileReader, Reader}
import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object CsvUtils {

  def csvApisToProcess(csvFilePath: String): Seq[CsvApiRecord] = {
    csvApisToProcessWithReader( new FileReader(csvFilePath))
  }

  def csvApisToProcessWithReader(in: Reader): Seq[CsvApiRecord] = {
    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords.asScala
      .map(createRecord).filter(_.isDefined)
      .map(x => { if(x.isDefined) Seq(x.get) else Seq.empty})
      .reduce( _++_)
      
  }

  private def createRecord(record: CSVRecord): Option[CsvApiRecord] = {
    def ramlPath = if (record.size() > 4) Some(record.get(4)) else None

   if(validateRecord(record)){
      Some( CsvApiRecord(
          name = record.get(0),
          version = record.get(2),
          accessType = AccessType(Option(record.get(3))),
          ramlPathOverride = ramlPath
        )
      )
   } else None
  }

  private def validateRecord(record: CSVRecord) = {
    // has minimimum fields?
    if(!validateField(0, record))       println(s"problem with Name field")
    if(!validateField(2, record))       println(s"problem with Version field")
    if(!validateField(3, record))       println(s"problem with AccessType field")

     if(record.size < 4 ) 
      false else 
     true
  }

  private def validateField(fieldNumber: Int, record: CSVRecord) = {
    Try{
      record.get(fieldNumber)
    } match{
      case Success(value) => true
      case Failure(e) => false
    }
  }
}
