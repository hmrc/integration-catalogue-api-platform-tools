import amf.client.model.domain.WebApi

import java.io.FileReader
import org.apache.commons.csv.CSVRecord

import scala.collection.JavaConverters._
import webapi.{Oas30, Raml10, WebApiBaseUnit, WebApiDocument}

import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.concurrent.java8.FuturesConvertersImpl


object Main extends App {

  object AccessType {
    def apply(text: Option[String]) : AccessType = {
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
  sealed trait AccessType;
  case class Public() extends AccessType
  case class Private() extends AccessType

  case class Api(name: String, version: String, accessType: AccessType, ramlPath : Option[String])

  def createRecord(record: CSVRecord) : Api = {

    def ramlPath = 
      if (record.size() > 4) {
        Some(record.get(4))
      } else None


    Api(
      record.get(0),
      record.get(2),
      AccessType(Option(record.get(3))),
      ramlPath
    )
  }

  val in = new FileReader("apis-to-import.csv")

  val apisToProcess = 
    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords().asScala.toSeq
      .map(createRecord)

      
      
  // def getGitCloneCommands(apisToProcess : Seq[Api]) = {
  //   apisToProcess
  //     .map(api => s"git clone git@github.com:hmrc/${api.name}.git")
  //     .mkString("\n")
  // }

  // println(getGitCloneCommands(apisToProcess))

  def parseRamlFromGitRepo(apis: Seq[Api]) : Unit = {

    def addAccessType(model : WebApiDocument, api: Api) = {

      val webApi : WebApi = model.encodes.asInstanceOf[WebApi]

      val accessTypeDescription = api.accessType match {
        case Public() => "This is a public API."
        case Private() => "This is a private API."
      }

      

      Option(webApi.description) match {
        case None => webApi.withDescription(accessTypeDescription)
        case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
        case Some(currentDescription) => webApi.withDescription(webApi.description + s" $accessTypeDescription")
      }
      // webApi.withDescription(webApi.description + s" $text")
    }

    def tryParseFile(api: Api, filename: String) : Try[Unit] = {
      Try({
        // println(s"Starting API: ${api.name}, ${api.version} filename:\n${filename}")

        val model : WebApiDocument = Raml10.parse(filename).get().asInstanceOf[WebApiDocument]

        val outputFilepath = s"file://generated/${api.name}-${api.version}.yaml"

        addAccessType(model, api)
//        val webApi : WebApi = model.encodes.asInstanceOf[WebApi]
//        webApi.withName("cheese")
//
//        webApi.withDescription(webApi.description + "\nThis is a public API.")
//
//        println(api.name)

        val f = Oas30.generateYamlFile(model, outputFilepath)
        f.get(60, TimeUnit.SECONDS)

        // Oas30.generateFile(model, outputFilepath)

        // println("Generated Oas30 YAML file at: " + outputFilepath); 
      })
    }

    apis.foreach(api => {

      val ramlPath = api.ramlPath.getOrElse("resources/public/api/conf")

      val filename = s"file://api-repos/${api.name}/$ramlPath/${api.version}/application.raml"

      val result = tryParseFile(api,filename);

      result match {
        case Failure(exception) => {
          println(s"failed: ${api.name}, ${api.version} - filename: ${filename} ${exception.toString}")
        }
        case Success(value) => Unit 
      }
    })
  }

  parseRamlFromGitRepo(apisToProcess)

  // ApiPlatformOasExport.export()
}

