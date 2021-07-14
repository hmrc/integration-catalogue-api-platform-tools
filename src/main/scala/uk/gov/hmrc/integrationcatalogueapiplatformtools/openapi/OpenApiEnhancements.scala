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
import io.swagger.v3.parser.core.models.ParseOptions
import uk.gov.hmrc.integrationcatalogueapiplatformtools.Logging
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.ConvertedWebApiToOasResult

import java.util

import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.OpenApiProcessingError
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.GeneralOpenApiProcessingError
import io.swagger.v3.oas.models.ExternalDocumentation

trait OpenApiEnhancements extends ExtensionKeys with Logging with ValidateXamfText with OpenAPICommon {

  def addOasSpecAttributes(convertedOasResult: ConvertedWebApiToOasResult): Either[OpenApiProcessingError, String] = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
    val validatedOpenApi = Option(new OpenAPIV3Parser().readContents(convertedOasResult.oasAsString, new util.ArrayList(), options))
      .flatMap(swaggerParseResult => Option(swaggerParseResult.getOpenAPI)) match {
      case Some(openApi) => validateAmfOAS(openApi, convertedOasResult.apiName)
      case None => Left(GeneralOpenApiProcessingError(convertedOasResult.apiName, "Swagger Parse failure"))
    }

    validatedOpenApi match {
      case Right(openAPI) => {
        addAccessTypeToDescription(openAPI, convertedOasResult.accessTypeDescription)
          .flatMap(addExtensions(_, convertedOasResult.apiName))
          .map(concatenateXamfDescriptions)
          .map(addCommonHeaders(convertedOasResult.apiName, _))
          .map((x => Right(openApiToContent(x))))
          .getOrElse(Left(GeneralOpenApiProcessingError(convertedOasResult.apiName, "Swagger Parse failure")))
      }
      case Left(e: OpenApiProcessingError) => Left(e)
    }
  }

  def addCommonHeaders(apiName: String, openApi: OpenAPI): OpenAPI ={
    addOperationLevelHeaders(openApi)
    openApi
  }

  private def addAccessTypeToDescription(openApi: OpenAPI, accessTypeDescription: String): Option[OpenAPI] = Option(openApi.getInfo).map(info => {
    Option(info.getDescription) match {
      case None => info.setDescription(accessTypeDescription)
      case Some(x) if (x.isEmpty || x == "null") => info.setDescription(accessTypeDescription)
      case Some(_) => ()
    }
    openApi.setInfo(info)
    openApi
  })

  private def fixDocContent(content: String): String = fixDevhubUrls(addNewLineToBulletMarkDownIfNeeded(content))

  def fixDevhubUrls(content: String) ={
    content.replaceAll("\\(/api-documentation/docs/", "(https://developer.service.hmrc.gov.uk/api-documentation/docs/")
  }

  def addNewLineToBulletMarkDownIfNeeded(content: String) ={
    content.replaceAll("(?<!\\n)(\\n){1}(\\*){1}( ){1}", "\n\n* ")
  }


  private def concatenateXamfDescriptions(openAPI: OpenAPI): OpenAPI = {
    def extractExternalDocsContent(externalDocs: ExternalDocumentation): Option[String] = {
      val description = Option(externalDocs.getDescription)
      val title = Option(externalDocs.getExtensions)
        .flatMap(x => Option(x.get(X_AMF_TITLE_KEY)))

      (title, description) match {
        case (Some(title), Some(description)) => Some("#  " + title + "\n" + description)
        case _ => None
      }
    }

    val longDesc = for {
      externalDocs <- Option(openAPI.getExternalDocs)
      externalDocsDesc = extractExternalDocsContent(externalDocs)
      extensions = getXamfDocumentationExtensions(openAPI)
      xamfDocsContent = if (extensions.isDefined) {
        extractDocumentation("N/A", extensions.get).map(doc => "#  " + doc.title + "\n" + fixDocContent(doc.content)).mkString("\n")
      } else ""
    } yield externalDocsDesc.getOrElse("") + "\n" + xamfDocsContent

    // look for "* " and check 4 characters before is /n/n or /n if /n make /n/n
    Option(openAPI.getInfo)
      .map(info => {
        longDesc.map(x => if (x.nonEmpty) {
          info.setDescription(x)
        })
      })
    openAPI

  }


  private def addExtensions(openApi: OpenAPI, apiName: String): Option[OpenAPI] = {
    val subLevelExtensions = new util.HashMap[String, AnyRef]()

    Option(openApi.getInfo)
      .map(info => {

        subLevelExtensions.put(PLATFORM_EXTENSION_KEY, "API_PLATFORM")
        subLevelExtensions.put(PUBLISHER_REF_EXTENSION_KEY, apiName)

        Option(info.getDescription).map(description => {
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
    if (description.length > 180) description.substring(0, 180 - 3) + "..."
    else description
  }

  private def openApiToContent(openApi: OpenAPI): String = {
    Yaml.mapper().writeValueAsString(openApi)
  }
}
