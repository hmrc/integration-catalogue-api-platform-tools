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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.openapi

import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.{ParseOptions, SwaggerParseResult}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.ConvertedWebApiToOasResult
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport.{EXTENSIONS_KEY, PLATFORM_EXTENSION_KEY, PUBLISHER_REF_EXTENSION_KEY}

import java.util
import scala.util.{Failure, Success, Try}

trait OpenApiEnhancements {

  def addOasSpecAttributes(convertedOasResult: ConvertedWebApiToOasResult): Option[String] = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
     Option(new OpenAPIV3Parser().readContents(convertedOasResult.oasAsString, null, options))
     .flatMap(swaggerParseResult => Option(swaggerParseResult.getOpenAPI))
      .map(addExtensions(_, convertedOasResult.apiName)
        .map(openApiToContent).getOrElse(""))
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
