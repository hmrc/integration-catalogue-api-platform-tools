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
                val description = a.asScala.get("description")
                val value = a.asScala.get("value")
                //  match {
                //   case Some(x: String) => x
                //   case Some(c: java.util.LinkedHashMap[String, String]) => c.asScala.values.mkString
                //   case Some(d: java.util.LinkedHashMap[String, java.util.ArrayList[String]]) => d.asScala.values.toList.mkString
                //   case Some(e) => e
                //   case None => ""
                // }
                val example = new Example()
                description.map(_.toString).map(example.setDescription)
                value.fold(example.setValue(a))(x=> example.setValue(x))
                mt.addExamples(exampleKey, example)
    }

/// map the schema values somewhere then copy back to mediatype
    content.values().asScala.map(mt => {
      val examples = Option(mt.getSchema).flatMap(x => Option(x.getExtensions()).flatMap(extensions => Option(extensions.get(X_AMF_EXAMPLES))))
      examples match {
        case Some(y: java.util.List[String])                  => println(y.asScala.mkString)
        case Some(y: java.util.ArrayList[java.util.LinkedHashMap[String, Object]]) => y.forEach(handleLinkHashMap(_, mt, "-"))
        case Some(z: java.util.LinkedHashMap[String, Object]) => z.asScala.foreach(x => {
            println(s"${x._1}")

            x._2 match {
              case a: java.util.LinkedHashMap[String, Object] => handleLinkHashMap(a, mt, x._1)
              
              case _                                          => ()
            }

            println(x._2.getClass.getName)
            println(x._2.toString)

          })
        case Some(y)                                          => println(s"***** NOT SURE WHAT EXAMPLE IS!!! ${y.getClass().getName()}")
        case None                                             => println(s"***** NOT SURE WHAT EXAMPLE IS!!! None")
      }
      // examples.foreach(example => {
      //     println(example.toString)
      //    //mt.setExample("blah")
      //     })

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
    openAPI
    println(s"getting examples for ${openAPI.getInfo().getTitle()}")
    Option(openAPI.getPaths())
      .map(paths =>
        paths.values
          .forEach(pathItem => pathItem.readOperations().asScala.map(handleOperation))
      )
    openAPI
    // paths -> pathitem -> Operation -> RequestBody/ApiResonpse -> Content (linked hashmap of "application/json" -> MediaType Object)
    // inside mediatype Schema -> extension "x-amf-examples"
    // copy the examples to MediaType > examples

  }
}
