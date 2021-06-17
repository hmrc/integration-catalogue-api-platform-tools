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

package uk.gov.hmrc.integrationcatalogueapiplatformtools.webapihandler

import amf.client.model.domain.WebApi
import uk.gov.hmrc.integrationcatalogueapiplatformtools.model.{ConvertedWebApiToOasResult, CsvApiRecord, Private, Public}
import webapi.{Oas30, Raml10, WebApiDocument}

import scala.compat.java8._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait WebApiHandler {

  // $COVERAGE-OFF$
  def parseRamlFromFileName(fileName: String): Future[WebApiDocument] = {
    FutureConverters.toScala(Raml10.parse(fileName)).map(x =>  x.asInstanceOf[WebApiDocument])
  }
 
  def parseOasFromWebApiModel(model: WebApiDocument, apiName: String): Future[ConvertedWebApiToOasResult] = {
    FutureConverters.toScala(Oas30.generateYamlString(model)).map(oasAsString => ConvertedWebApiToOasResult(oasAsString, apiName))
  }
  // $COVERAGE-ON$

  def addAccessTypeToDescription(model: WebApiDocument, api: CsvApiRecord): WebApi = {

    val webApi: WebApi = model.encodes.asInstanceOf[WebApi]

    val accessTypeDescription = api.accessType match {
      case Public()  => "This is a public API."
      case Private() => "This is a private API."
    }

    Option(webApi.description) match {
      case None                       => webApi.withDescription(accessTypeDescription)
      case Some(x) if x.isNullOrEmpty => webApi.withDescription(accessTypeDescription)
      case Some(_)                    => webApi.withDescription(webApi.description + s" $accessTypeDescription")
    }
  }

  def getFileNameForCsvRecord(csvApiRecord: CsvApiRecord): String = {
    val ramlPath = csvApiRecord.ramlPathOverride.getOrElse("resources/public/api/conf")
    s"file://api-repos/${csvApiRecord.name}/$ramlPath/${csvApiRecord.version}/application.raml"
  }

}
