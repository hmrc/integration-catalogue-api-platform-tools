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
import scala.collection.JavaConverters._
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.OpenApiProcessingError
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.GeneralOpenApiProcessingError
import io.swagger.v3.oas.models.ExternalDocumentation

trait OpenApiEnhancements extends ExtensionKeys with Logging {

  def addOasSpecAttributes(convertedOasResult: ConvertedWebApiToOasResult): Either[OpenApiProcessingError, String] = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
    val maybeOpenApi = Option(new OpenAPIV3Parser().readContents(convertedOasResult.oasAsString, null, options))
      .flatMap(swaggerParseResult => Option(swaggerParseResult.getOpenAPI))

    val validatedOpenApi = maybeOpenApi match {
      case Some(openApi) => validateAmfOAS(openApi, convertedOasResult.apiName)
      case None          => Left(GeneralOpenApiProcessingError(convertedOasResult.apiName, "Swagger Parse failure"))
    }

    validatedOpenApi match {
      case Right(openAPI)                  => {
        addAccessTypeToDescription(openAPI, convertedOasResult.accessTypeDescription)
          .flatMap(addExtensions(_, convertedOasResult.apiName))
          .map(concatenateXamfDescriptions(_))
          .map(x => Right(openApiToContent(x)))
          .getOrElse(Left(GeneralOpenApiProcessingError(convertedOasResult.apiName, "Swagger Parse failure")))
      }
      case Left(e: OpenApiProcessingError) => Left(e)
    }
  }

    def getExtensions(openApi: OpenAPI): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
      Option(openApi.getExtensions()).flatMap(extensionsMap =>
        Option(extensionsMap.get(X_AMF_USERDOCUMENTATION_KEY))
          .map(x => {
            x.asInstanceOf[util.ArrayList[java.util.LinkedHashMap[String, Object]]]
          })
      )
    }


    case class SubDocument(apiName: String, title: String, content: String)

  

    def extractDocumentation(apiName: String, extensionData: util.ArrayList[java.util.LinkedHashMap[String, Object]]) = {
      val convertedList = extensionData.asScala.toList
      convertedList.flatMap(x => {
        val maybeContent = Option(x.get("content"))
        val mayBeTitle = Option(x.get("title"))
        (mayBeTitle, maybeContent) match {
          case (Some(title: String), Some(content: String)) => Some(SubDocument(apiName, title, content))
          case _ => None
        }
      })
    }

  private def validateAmfOAS(openApi: OpenAPI, apiName: String): Either[OpenApiProcessingError, OpenAPI] = {


    def validateSubdocuments(subdocuments: List[SubDocument], openApi: OpenAPI) = {
      val errorListAsStrings: List[String] = subdocuments.filter(document => {
        document.content.contains("https://developer.service.hmrc.gov.uk/api-documentation/assets/")
      })
      .map(document => s"API: ${document.apiName} content for title: ${document.title} points to a file!! ${document.content} \n")

      if (errorListAsStrings.nonEmpty) {
        Left(GeneralOpenApiProcessingError(apiName, errorListAsStrings.mkString))
      } else Right(openApi)

    }

    val maybeUserDocumentationExtensions: Option[util.ArrayList[util.LinkedHashMap[String, AnyRef]]] =
      Option(openApi).flatMap(getExtensions)

    val listOfsubDocuments: List[SubDocument] =
      maybeUserDocumentationExtensions.map(x => extractDocumentation(apiName, x)).getOrElse(List.empty)

    validateSubdocuments(listOfsubDocuments, openApi)

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

  private def concatenateXamfDescriptions(openAPI: OpenAPI): OpenAPI ={
     def extractExternalDocsContent(externalDocs: ExternalDocumentation)={
        val description = Option(externalDocs.getDescription())
        val title = Option(externalDocs.getExtensions())
        .flatMap(x=> Option(x.get(X_AMF_TITLE_KEY)))
          
        (title, description) match {
              case (Some(title), Some(description)) => Some("#  " + title + "\n" + description)
              case _ => None
        }
     }
 
    val longDesc = for{
      externalDocs <- Option(openAPI.getExternalDocs())
      externalDocsDesc =  extractExternalDocsContent(externalDocs)
      extensions <-  getExtensions(openAPI)
      xamfDocsContent = extractDocumentation("N/A", extensions).map(doc =>"#  "+ doc.title + "\n" + doc.content).mkString("\n")
    } yield externalDocsDesc.getOrElse("") + "\n" +  xamfDocsContent
     
      Option(openAPI.getInfo())
      .map(info => {
        longDesc.map(x => if(x.nonEmpty){info.setDescription(x)})
      })
      openAPI
    // get external doc section and externalDocs ->  x-amf-title & description
    // get x-amf-documentation extensions... need to maintain order and concatenate "title" and "content"
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
