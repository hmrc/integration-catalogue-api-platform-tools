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

import io.swagger.v3.oas.models.OpenAPI
import scala.collection.JavaConverters._
import java.util
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter

trait OpenAPICommon extends ExtensionKeys {

  private def addCommonHeaders(openAPI: OpenAPI)={
    def addHeadersToOperation(operation: Operation) ={
      val contentTypeHeader = new Parameter()
      contentTypeHeader.setIn("header")
      contentTypeHeader.setName("Content-Type")
      contentTypeHeader.setDescription("Specifies the format of the request body, which must be JSON.")
      contentTypeHeader.setRequired(true)

      val headers = new java.util.ArrayList[Parameter]()
      headers.add(contentTypeHeader)
      operation.setParameters(headers)
      operation
    }
    Option(openAPI.getPaths)
      .map(paths => paths.asScala.toMap.map(x => x._2)
      .map(x => x.readOperations().asScala.foreach(addHeadersToOperation(_))))

    openAPI
  }


  def extractUses(apiName: String, extensionData: util.ArrayList[java.util.LinkedHashMap[String, Object]], openAPI: OpenAPI) = {
    val convertedList = extensionData.asScala.toList
    convertedList.flatMap(x => {
      val maybeHeaders = Option(x.get("headers"))
      val maybeSec = Option(x.get("sec"))
      maybeHeaders.map(x => {
        if (x.toString == "https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/headers.raml") {
          addCommonHeaders(openAPI)
          println(s"$apiName headers are common ones")

        } else { println(s"$apiName non standard headers or missing from x-amf-uses") }
      })

      maybeSec.map(x => {
        if (x.toString == "https://developer.service.hmrc.gov.uk/api-documentation/assets/common/modules/securitySchemes.raml") {
          println(s"$apiName security is common one")
        } else { println(s"$apiName non standard security or missing from x-amf-uses") }
      })
    })
    openAPI
  }

  def addAcceptAndContentTypeHeaders(openApi: OpenAPI): OpenAPI = {
    openApi
  }

  def addAuthorizationHeader(openApi: OpenAPI): OpenAPI = {
    openApi
  }

  def extractDocumentation(apiName: String, extensionData: util.ArrayList[java.util.LinkedHashMap[String, Object]]): List[SubDocument] = {
    val convertedList = extensionData.asScala.toList
    convertedList.flatMap(x => {
      val maybeContent = Option(x.get("content"))
      val mayBeTitle = Option(x.get("title"))
      (mayBeTitle, maybeContent) match {
        case (Some(title: String), Some(content: String)) => Some(SubDocument(apiName, title, content))
        case _                                            => None
      }
    })
  }

  private def getExtensions(openApi: OpenAPI, key: String): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    Option(openApi.getExtensions()).flatMap(extensionsMap =>
      Option(extensionsMap.get(key))
        .map(x => {
          x match {
            case _ if (x.isInstanceOf[util.ArrayList[java.util.LinkedHashMap[String, Object]]]) => x.asInstanceOf[util.ArrayList[java.util.LinkedHashMap[String, Object]]]
            case _ if (x.isInstanceOf[java.util.LinkedHashMap[String, Object]])                 => {
              val list = new util.ArrayList[java.util.LinkedHashMap[String, Object]]()
              list.add(x.asInstanceOf[java.util.LinkedHashMap[String, Object]])
              list
            }
          }

        })
    )
  }

  def getXamfDocumentationExtensions(openApi: OpenAPI): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    getExtensions(openApi, X_AMF_USERDOCUMENTATION_KEY)
  }

  def getXamfUsesExtensions(openApi: OpenAPI): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    getExtensions(openApi, X_AMF_USES)
  }

}
