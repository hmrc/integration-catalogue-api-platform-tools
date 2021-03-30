import webapi.Raml10;
import webapi.Oas30;
import webapi.WebApiModule;

import java.util.concurrent.ExecutionException;
import webapi.WebApiBaseUnit
import javax.xml.transform.Source
import java.io.BufferedWriter
import java.io.FileWriter

object ApiPlatformOasExport {
  def export() = {

    ListOfApis.apis.
      foreach(createApiPlatformOas)

    println("Done!")
  }

  def createApiPlatformOas(api: Api){
    // QA
    //val url = s"https://developer.qa.tax.service.gov.uk/api-documentation/docs/api/download/${api.serviceName}/${api.version}/application.raml";

    // Prod
    val url = s"https://developer.service.hmrc.gov.uk/api-documentation/docs/api/download/${api.serviceName}/${api.version}/application.raml";

    try{
      val model : WebApiBaseUnit = Raml10.parse(url).get()

      val outputFilepath = s"file://generated/${api.serviceName}-${api.version}.yaml"
      Oas30.generateYamlFile(model, outputFilepath)
      // Oas30.generateFile(model, outputFilepath)

      println("Generated Oas30 YAML file at: " + outputFilepath);      
    } catch {
      case e : Throwable => println(s"failed: ${api.serviceName}, ${api.version} - ${e.toString}")
    }
  }
}
