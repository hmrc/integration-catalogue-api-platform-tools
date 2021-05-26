package uk.gov.hmrc.integrationcatalogueapiplatformtools.apidocsdownload

import amf.client.model.document.BaseUnit
import amf.client.parse.Raml10Parser
import amf.client.render.Oas20Renderer
import amf.client.resolve.Oas20Resolver
import amf.core.resolution.pipelines.ResolutionPipeline

import java.util.concurrent.ExecutionException
import javax.xml.transform.Source
import java.io.BufferedWriter
import java.io.FileWriter
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit


object ApiPlatformOasExport {
  def export() = {

    ListOfApis.apis.
      foreach(api => {
        java.util.concurrent.TimeUnit.SECONDS.sleep(1); // To prevent akamai from blocking requests due to DDos protected. TBC.
        createApiPlatformOas(api)
      })

    println("Done!")
  }

  def createApiPlatformOas(api: Api){
    // QA
    //val url = s"https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/download/${api.serviceName}/${api.version}/application.raml";

    // Prod
    val url = s"https://developer.service.hmrc.gov.uk/api-documentation/docs/api/download/${api.serviceName}/${api.version}/application.raml";

    try{

      val parser = new Raml10Parser

      val model: BaseUnit = parser.parseFileAsync(url).get


      val outputFilepath = s"file://generated/${api.serviceName}-${api.version}.yaml"
      println(s"Starting API: ${api.serviceName}, ${api.version} Url:\n${url}")

      val resolver = new Oas20Resolver
      val renderer = new Oas20Renderer

      val convertedOasApi = resolver.resolve(model, ResolutionPipeline.DEFAULT_PIPELINE)

      renderer.generateFile(convertedOasApi,outputFilepath).get

//      val f = Oas30.generateYamlFile(model, outputFilepath)
//      f.get(60, TimeUnit.SECONDS)
            
      // Oas30.generateFile(model, outputFilepath)

      println("Generated Oas30 YAML file at: " + outputFilepath);      
    } catch {
      case e : Throwable => println(s"createApiPlatformOas failed: ${api.serviceName}, ${api.version} - ${e.toString}")
    }
  }
}
