package uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.CsvApiRecord
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.Public
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.Private
import amf.client.model.domain.WebApi
import webapi.WebApiDocument
import webapi.{Oas30, Raml10, WebApiBaseUnit, WebApiDocument}
import scala.compat.java8._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait WebApiHandler {

  def parseRamlFromFileName(fileName: String) = {
    FutureConverters.toScala(Raml10.parse(fileName)).map(_.asInstanceOf[WebApiDocument])
  }

  def parseOasFromWebApiModel(model: WebApiDocument) = {
    FutureConverters.toScala(Oas30.generateYamlString(model))
  }

// def tryParseFile(csvApiRecord: CsvApiRecord, filename: String): String = {
//  Try({
//   val apiName = csvApiRecord.name

//  val outputFilepath = s"generated/${csvApiRecord.name}.yaml"

//    val fYamlString =  for{
//       model <-  parseRamlFromFileName(filename)
//       yamlString <- parseOasFromWebApiModel(model)

//     } yield yamlString
//      fYamlString.map(yamlString =>
//      addOasSpecAttributes(yamlString, apiName) match {
//             case Some(openApiAsString) => writeToFile(outputFilepath, openApiAsString)
//             case None                  => writeYamlFile(model, outputFilepath)
//           }
//     )
//  )}
//  }

  def addAccessTypeToDescription(model: WebApiDocument, api: CsvApiRecord) = {

    val webApi: WebApi = model.encodes.asInstanceOf[WebApi]

    val accessTypeDescription = api.accessType match {
      case Public()  => "This is a public API."
      case Private() => "This is a private API."
    }

    Option(webApi.description) match {
      case None                       => webApi.withDescription(accessTypeDescription)
      case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
      case Some(_)                    => webApi.withDescription(webApi.description + s" $accessTypeDescription")
    }
  }

  def getFileNameForCsvRecord(fCsvApiRecord: Future[CsvApiRecord]) = {
    fCsvApiRecord.map({ csvApiRecord => 
    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    s"file://api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"
    }) 
  }

  def parseRaml(csvApiRecord: CsvApiRecord): Unit = {
  

    // tryParseFile(csvApiRecord, filename) match {
    //   case Failure(exception) =>
    //     println(s"failed: ${csvApiRecord.name}, ${csvApiRecord.version} - filename: $filename ${exception.toString}")
    //   case Success(_)     => Unit
    // }
  }
}
