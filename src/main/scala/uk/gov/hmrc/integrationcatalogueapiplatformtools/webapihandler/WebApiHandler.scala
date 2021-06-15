package uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.{ConvertedWebApiToOasResult, CsvApiRecord, Private, Public}
import amf.client.model.domain.WebApi
import webapi.{Oas30, Raml10, WebApiBaseUnit, WebApiDocument}

import java.nio.file.Paths
import scala.compat.java8._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import org.slf4j.LoggerFactory
import org.apache.commons.logging.impl.Log4JLogger
import com.typesafe.scalalogging.Logger



trait WebApiHandler {

  lazy val webApiHandlerLogger = Logger(LoggerFactory.getLogger("WebApiHandler"))

  def parseRamlFromFileName(fileName: String): Future[WebApiDocument] = {
//    val model: WebApiDocument = Raml10.parse(fileName).get().asInstanceOf[WebApiDocument]
//    webApiHandlerLogger.info(s"********* In parseRamlFromFileName - ${model.toString}")
//
//

     FutureConverters.toScala(Raml10.parse(fileName))
     .map(x=>  { println(x.toString)
                                 x.asInstanceOf[WebApiDocument]})

//     Raml10.parse(fileName).get().asInstanceOf[WebApiDocument]

  }

  def parseOasFromWebApiModel(model: WebApiDocument, apiName: String): Future[ConvertedWebApiToOasResult] = {
    FutureConverters.toScala(Oas30.generateYamlString(model)).map(oasAsString => ConvertedWebApiToOasResult(oasAsString, apiName))
  
  }

  def addAccessTypeToDescription(model: WebApiDocument, api: CsvApiRecord): WebApi = {

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

  def getFileNameForCsvRecord(csvApiRecord: CsvApiRecord): String = {
    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    val absolutePath =Paths.get(".").toAbsolutePath.toString.replace(".","")
    println(absolutePath)
    s"file://${absolutePath}api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"
  }


}
