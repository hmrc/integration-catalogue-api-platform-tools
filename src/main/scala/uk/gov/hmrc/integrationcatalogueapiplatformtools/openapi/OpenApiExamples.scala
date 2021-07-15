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
import io.swagger.v3.oas.models.PathItem
import scala.collection.JavaConverters._
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.MediaType

trait OpenApiExamples extends ExtensionKeys {


  def handleContent(content: Content) = {

    def handleLinkHashMap(a: java.util.LinkedHashMap[String, Object], mt: MediaType, exampleKey: String) ={
                val example = new Example()
                a.asScala.get("description").map(_.toString).map(example.setDescription)
                a.asScala.get("value").fold(example.setValue(a))(x=> example.setValue(x))
                mt.addExamples(exampleKey, example)
    }

    content.values().asScala.map(mt => {
      val examples = Option(mt.getSchema).flatMap(x => Option(x.getExtensions()).flatMap(extensions => Option(extensions.get(X_AMF_EXAMPLES))))
      examples match {
        case Some(z: java.util.LinkedHashMap[String, Object]) => z.asScala.foreach(x => {
            x._2 match {
              case a: java.util.LinkedHashMap[String, Object] => handleLinkHashMap(a, mt, x._1)
              case _                                          => ()
            }
          })
        case Some(_)                                          => println(s"EXAMPLE IS Unknown Type")  
        case None                                             => println(s"EXAMPLE IS None")
      }
    })
    ()
  }

  def handleOperation(operation: Operation) = {
    Option(operation.getRequestBody())
      .map(request => Option(request.getContent).map(handleContent))

    Option(operation.getResponses())
      .map(responses =>
        responses.values
          .forEach(response => Option(response.getContent).map(handleContent))
      )
  }

  def addExamples(openAPI: OpenAPI) = {
    println(s"getting examples for ${openAPI.getInfo().getTitle()}")
    Option(openAPI.getPaths())
      .map(paths =>
        paths.values
          .forEach(pathItem => pathItem.readOperations().asScala.map(handleOperation))
      )
    openAPI
  }
}
