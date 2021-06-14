package uk.gov.hmrc.integrationcatalogueapiplatformtools.model

sealed trait AccessType

case class Public() extends AccessType

case class Private() extends AccessType

object AccessType {

  def apply(text: Option[String]): AccessType = {
    text.map(_.toUpperCase()) match {
      case None            => Public()
      case Some("PUBLIC")  => Public()
      case Some("PRIVATE") => Private()
      case Some("BOTH")    => Public()
      case Some("")        => Public()
      case other           => throw new RuntimeException(s"Unknown accessType: $other")
    }
  }
}

case class CsvApiRecord(name: String, version: String, accessType: AccessType, ramlPathOverride: Option[String])