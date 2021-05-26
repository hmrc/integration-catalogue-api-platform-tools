package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

import amf.client.convert.CoreClientConverters.ClientOption
import amf.client.model.document.BaseUnit
import amf.client.parse._
import amf.client.render.{Oas20Renderer, Oas30Renderer, RenderOptions}
import amf.client.resolve.{ClientErrorHandler, Oas20Resolver, Oas30Resolver}
import amf.core.client.ParsingOptions
import amf.core.resolution.pipelines.ResolutionPipeline
import org.apache.commons.csv.CSVRecord

import java.io.FileReader
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}

sealed trait AccessType;

case class Public() extends AccessType

case class Private() extends AccessType

object AccessType {
  def apply(text: Option[String]): AccessType = {
    text.map(_.toUpperCase()) match {
      case None => Public()
      case Some("PUBLIC") => Public()
      case Some("PRIVATE") => Private()
      case Some("BOTH") => Public()
      case Some("") => Public()
      case other => throw new RuntimeException(s"Unknown accessType: $other")
    }
  }
}

case class CsvApiRecord(name: String, version: String, accessType: AccessType, ramlPathOverride: Option[String])

object RepoFileExport {

  class UnhandledErrorHandler extends ClientErrorHandler {

    override def reportConstraint(id: String,
                                  node: String,
                                  property: ClientOption[String],
                                  message: String,
                                  range: ClientOption[amf.core.parser.Range],
                                  level: String,
                                  location: ClientOption[String]) {

      val msg: String = String.format(
        "Message: %s\nTarget: %s\nProperty: %s\n at location: %s",
        message, node, property.orElse(""),  location.orElse("")
      )

      throw new RuntimeException(msg);
    }
  }

  def generateOasFiles()(implicit ec: ExecutionContext) = {
    println("**** HELLO *** ")
    csvApisToProcess.foreach(parseRaml)
  }

  def csvApisToProcess(): Seq[CsvApiRecord] = {
    val in = new FileReader("api-definition-csv-export.csv")

    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords().asScala.toSeq
      .map(createRecord)
  }

  private def tryParseFile(csvApiRecord: CsvApiRecord, filename: String)(implicit ec: ExecutionContext): Try[Unit] = Try({
    println("IN CODE!!!!")
    val outputFilepath = s"file://generated/${csvApiRecord.name}.yaml"

    val parser = new Raml10Parser()
    val resolver = new Oas30Resolver()
    val renderer = new Oas30Renderer()


    val parseOptions = ParsingOptions().withoutAmfJsonLdSerialization
    val ramlApi: BaseUnit = parser.parseFileAsync(filename, parseOptions).get()

    println(s"rendering parsed Raml ${ramlApi.toString}")
    val convertedOasApi =  resolver.resolve(ramlApi, ResolutionPipeline.DEFAULT_PIPELINE, new UnhandledErrorHandler)




    val renderOptions = RenderOptions().withoutAmfJsonLdSerialization
    println(s"rendering result ${convertedOasApi.toString}")
    renderer.generateFile(convertedOasApi, outputFilepath, renderOptions).get


  })

  private def createRecord(record: CSVRecord): CsvApiRecord = {
    def ramlPath = if (record.size() > 4) Some(record.get(4)) else None

    CsvApiRecord(
      name = record.get(0),
      version = record.get(2),
      accessType = AccessType(Option(record.get(3))),
      ramlPathOverride = ramlPath
    )
  }

  //  def addAccessTypeToDescription(model : BaseUnit, api: CsvApiRecord) = {
  //
  //    val webApi : WebApi = model
  //
  //    val accessTypeDescription = api.accessType match {
  //      case Public() => "This is a public API."
  //      case Private() => "This is a private API."
  //    }
  //
  //    Option(webApi.description) match {
  //      case None => webApi.withDescription(accessTypeDescription)
  //      case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
  //      case Some(currentDescription) => webApi.withDescription(webApi.description + s" $accessTypeDescription")
  //    }
  //  }

  private def parseRaml(csvApiRecord: CsvApiRecord)(implicit ec: ExecutionContext): Unit = {
    println(s"parsing $csvApiRecord")
    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    val filename = s"file://api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"

    tryParseFile(csvApiRecord, filename) match {
      case Failure(exception) => {
        exception.printStackTrace()
        println(s"parse Raml failed: ${csvApiRecord.name}, ${csvApiRecord.version} - filename: ${filename} ${exception.toString}")
      }
      case Success(_) => {
        println("seems ok!!")
        Unit
      }
    }
  }
}
