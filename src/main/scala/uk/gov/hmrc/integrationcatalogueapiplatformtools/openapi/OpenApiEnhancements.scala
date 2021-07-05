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

import amf.client.model.domain.WebApi
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.{ParseOptions, SwaggerParseResult}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.{ConvertedWebApiToOasResult, CsvApiRecord, Private, Public}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.repos.RepoFileExport.{EXTENSIONS_KEY, PLATFORM_EXTENSION_KEY, PUBLISHER_REF_EXTENSION_KEY}
import uk.gov.hmrc.integrationcatalogueapiplatformtools.Logging

import webapi.WebApiDocument

import java.util
import scala.util.{Failure, Success, Try}

trait OpenApiEnhancements extends ExtensionKeys with Logging {

  def addOasSpecAttributes(convertedOasResult: ConvertedWebApiToOasResult): Option[String] = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
    Option(new OpenAPIV3Parser().readContents(convertedOasResult.oasAsString, null, options))
      .flatMap(swaggerParseResult => Option(swaggerParseResult.getOpenAPI))
      .map(logXamfUserDocumentationStatus)
      .flatMap(addAccessTypeToDescription(_, convertedOasResult.accessTypeDescription)
        .map(addExtensions(_, convertedOasResult.apiName)
          .map(openApiToContent).getOrElse("")))
  }

 private def logXamfUserDocumentationStatus(openApi: OpenAPI)={
   val maybeOpenApi = Option(openApi)
   val maybeExtensions = maybeOpenApi
   .flatMap(openApi => Option(openApi.getExtensions()).flatMap(extensionsMap => Option(extensionsMap.get("x-amf-userDocumentation")).map(_.asInstanceOf[util.Map[String, Object]])))

  val maybeContentAsString =  maybeExtensions.flatMap(xMap => Option(xMap.get("content")).map(_.asInstanceOf[String]) )
  maybeContentAsString.map(content => {
    if(content.contains("https://developer.service.hmrc.gov.uk/api-documentation/assets/")){
        val apiName = maybeOpenApi.flatMap(x => Option(x.getInfo()).flatMap(x => Option(x.getTitle()))).getOrElse("UNABLE TO GET API NAME")
        logger.error(s"API: $apiName content points to file")
    } 
  })

   openApi
 }

  private def addAccessTypeToDescription(openApi: OpenAPI, accessTypeDescription: String): Option[OpenAPI] = {

    Option(openApi.getInfo).map(info => {
      Option(info.getDescription) match {
        case None                                  => info.setDescription(accessTypeDescription)
        case Some(x) if (x.isEmpty || x == "null") => info.setDescription(accessTypeDescription)
        case Some(_)                               => ()
      }
      openApi.setInfo(info)
      openApi
    })

  }

  private def addExtensions(openApi: OpenAPI, apiName: String): Option[OpenAPI] = {
    val subLevelExtensions = new util.HashMap[String, AnyRef]()

    Option(openApi.getInfo())
      .map(info => {

        subLevelExtensions.put(PLATFORM_EXTENSION_KEY, "API_PLATFORM")
        subLevelExtensions.put(PUBLISHER_REF_EXTENSION_KEY, apiName)

        Option(info.getDescription()).map(description => {
          subLevelExtensions.put(SHORT_DESC_EXTENSION_KEY, truncateShortDescription(description))
        })

      })

    val topLevelExtensionsMap = new util.HashMap[String, AnyRef]()
    topLevelExtensionsMap.put(EXTENSIONS_KEY, subLevelExtensions)

    Option(openApi.getInfo).map(info => {
      info.setExtensions(topLevelExtensionsMap)
      openApi.setInfo(info)
      openApi
    })

  }

  private def truncateShortDescription(description: String): String = {
    if (description.length > 180) description.substring(0, (180 - 3)) + "..."
    else description
  }

  private def openApiToContent(openApi: OpenAPI): String = {
    Yaml.mapper().writeValueAsString(openApi)
  }
}
