package uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi

import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.{ParseOptions, SwaggerParseResult}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.ConvertedWebApiToOasResult
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport.{EXTENSIONS_KEY, PLATFORM_EXTENSION_KEY, PUBLISHER_REF_EXTENSION_KEY}

import org.slf4j.LoggerFactory
import org.apache.commons.logging.impl.Log4JLogger
import com.typesafe.scalalogging.Logger
import java.util
import scala.util.{Failure, Success, Try}

trait OpenApiEnhancements {

  lazy val openApiEnhancementsLogger = Logger(LoggerFactory.getLogger("OpenApiEnhancements"))

  def addOasSpecAttributes(convertedOasResult: ConvertedWebApiToOasResult): Option[String] = {
    openApiEnhancementsLogger.info("in addOasSpecAttributes Start")
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
    val maybeSwaggerParseResult: Option[SwaggerParseResult] = try {
      openApiEnhancementsLogger.info("in addOasSpecAttributes 2")
      val parser = new OpenAPIV3Parser() 
      openApiEnhancementsLogger.info(s"have we got the yaml string?!? ${convertedOasResult.oasAsString}")
      openApiEnhancementsLogger.info(s"have we got a parser?!? ${parser.toString}")
     val results = parser.readContents(convertedOasResult.oasAsString, null, options)
     openApiEnhancementsLogger.info(s"***** *** *** *** *** ** ${results.getMessages()}")
       Option(results)
    } catch {
      case e: Exception => {
        openApiEnhancementsLogger.error(e.getMessage())
        e.printStackTrace
        None
      }
      case _ =>  {
        openApiEnhancementsLogger.error("ERRRRORORRORORORROROROR")
        None
      }
    }
    openApiEnhancementsLogger.info(s"***** In addOasSpecAttributes - maybeSwaggerParseResult: ${maybeSwaggerParseResult}")
    val result = maybeSwaggerParseResult.flatMap(swaggerParseResult => Option(swaggerParseResult.getOpenAPI))
      .map(addExtensions(_, convertedOasResult.apiName)
        .map(openApiToContent).getOrElse(""))

    openApiEnhancementsLogger.info("in addOasSpecAttributes End")
    result
  }

  private def addExtensions(openApi: OpenAPI, apiName: String): Option[OpenAPI] = {
    val subLevelExtensions = new util.HashMap[String, AnyRef]()

    subLevelExtensions.put(PLATFORM_EXTENSION_KEY, "API_PLATFORM")
    subLevelExtensions.put(PUBLISHER_REF_EXTENSION_KEY, apiName)

    val topLevelExtensionsMap = new util.HashMap[String, AnyRef]()
    topLevelExtensionsMap.put(EXTENSIONS_KEY, subLevelExtensions)

    Option(openApi.getInfo).map(info => {
      info.setExtensions(topLevelExtensionsMap)
      openApi.setInfo(info)
      openApi
    })

  }

  private def openApiToContent(openApi: OpenAPI): String = {
    Yaml.mapper().writeValueAsString(openApi)
  }
}
