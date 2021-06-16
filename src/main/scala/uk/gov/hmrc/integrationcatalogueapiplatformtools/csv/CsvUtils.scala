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

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model._
import org.apache.commons.csv.CSVRecord

import java.io.{FileReader, Reader}
import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger

object CsvUtils {
  lazy val logger = Logger(LoggerFactory.getLogger("CsvUtils"))

  def csvApisToProcess(csvFilePath: String): Seq[CsvApiRecord] = csvApisToProcessWithReader(new FileReader(csvFilePath))

  def csvApisToProcessWithReader(in: Reader): Seq[CsvApiRecord] = {

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords.asScala
      .map(createRecord)
      .filter(_.isDefined)
      .map(x => { if (x.isDefined) Seq(x.get) else Seq.empty[CsvApiRecord] })
      .reduce(_ ++ _)

  }

  private def createRecord(record: CSVRecord): Option[CsvApiRecord] = {
    def ramlPath = if (record.size() > 4) Some(record.get(4)) else None

    if (validateRecord(record)) {
      Some(CsvApiRecord(
        name = record.get(0),
        version = record.get(2),
        accessType = AccessType(Option(record.get(3))),
        ramlPathOverride = ramlPath
      ))
    } else None

  }

  private def validateRecord(record: CSVRecord) = {
    // has minimimum fields?
    if (!validateField(0, record)) logger.info(s"problem with Name field")
    if (!validateField(2, record)) logger.info(s"problem with Version field")
    if (!validateField(3, record)) logger.info(s"problem with AccessType field")

    if (record.size < 4)
      false
    else
      true
  }

  private def validateField(fieldNumber: Int, record: CSVRecord) = {
    Try {
      record.get(fieldNumber)
    } match {
      case Success(value) => true
      case Failure(e)     => false
    }
  }
}
