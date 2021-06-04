package openapi

import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.{ParseOptions, SwaggerParseResult}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport.{EXTENSIONS_KEY, PLATFORM_EXTENSION_KEY, PUBLISHER_REF_EXTENSION_KEY}

import java.util.HashMap
import java.util.concurrent.{CompletableFuture, TimeUnit}
import scala.collection.JavaConverters._

trait OpenApiEnhancements {

  def addOasSpecAttributes(yamlFileAsString: CompletableFuture[String], apiName: String): Option[String] = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
    val maybeSwaggerParseResult: Option[SwaggerParseResult] = Option(new OpenAPIV3Parser().readContents(yamlFileAsString.get(60, TimeUnit.SECONDS), null, options))
    maybeSwaggerParseResult.flatMap(swaggerParseResult =>
      (Option(swaggerParseResult.getOpenAPI), getListSafe(swaggerParseResult.getMessages)) match {
        case (Some(openApi), _) => addExtensions(openApi, apiName)
      })
  }

  private def getListSafe(list: java.util.List[String]): List[String] = {
    Option(list)
      .map(e => e.asScala.toList)
      .getOrElse(List.empty)
  }

  private def addExtensions(openApi: OpenAPI, apiName: String): Option[String] = {
    val subLevelExtensions = new HashMap[String, AnyRef]()

    subLevelExtensions.put(PLATFORM_EXTENSION_KEY, "API_PLATFORM")
    subLevelExtensions.put(PUBLISHER_REF_EXTENSION_KEY, apiName)

    val topLevelExtensionsMap = new HashMap[String, AnyRef]()
    topLevelExtensionsMap.put(EXTENSIONS_KEY, subLevelExtensions)

    Option(openApi.getInfo) match {
      case Some(info) => {
        info.setExtensions(topLevelExtensionsMap)
        openApi.setInfo(info)
        Some(openApiToContent(openApi))
      }
    }
  }

  private def openApiToContent(openApi: OpenAPI): String = {
    Yaml.mapper().writeValueAsString(openApi)
  }
}
