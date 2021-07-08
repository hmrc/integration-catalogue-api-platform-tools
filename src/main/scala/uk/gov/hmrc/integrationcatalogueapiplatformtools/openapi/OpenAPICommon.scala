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

trait OpenAPICommon extends ExtensionKeys {


  def extractDocumentation(apiName: String, extensionData: util.ArrayList[java.util.LinkedHashMap[String, Object]]): List[SubDocument] = {
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

  def getExtensions(openApi: OpenAPI): Option[util.ArrayList[java.util.LinkedHashMap[String, Object]]] = {
    Option(openApi.getExtensions()).flatMap(extensionsMap =>
      Option(extensionsMap.get(X_AMF_USERDOCUMENTATION_KEY))
        .map(x => {
          x.asInstanceOf[util.ArrayList[java.util.LinkedHashMap[String, Object]]]
        })
    )
  }


}
