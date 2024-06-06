/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.respository

import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable.{DataSource, FilterSpec, SortSpec, TblHead}

import java.lang.Math.min
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiAccessor @Inject()(ws: WSClient)(implicit ec:ExecutionContext) {

  abstract case class ApiDataSource[T<:Product] (endPoint: String, responsePath: String = "data", totalSizePath: String = "totalSize") extends DataSource[T] {
    def fetch(filter: Seq[FilterSpec], sortSpecs: Seq[SortSpec], page: Int, pageSize: Int = 20): Future[Data[T]] = {
      val filtParams = filter.map{ filter => ("filter" -> s"${filter.field.symbol}${filter.operation.symbol}${filter.needle}") }
      val sortParams = sortSpecs.map{ spec => ("sort" -> s"${if(spec.descending)"desc" else "asc"}-${spec.field.symbol}") }
      val pageParams = Seq("offset" -> s"${page*pageSize}", "limit" -> s"$pageSize" )
      ws.url(endPoint).withQueryStringParameters(
        (filtParams ++ sortParams ++ pageParams):_*
      ).get().map { response =>
        val totalSize = locateTotalSize(response.json)
        val numPages = totalSize/pageSize + min(1, totalSize % pageSize)
        Data(numPages, page, locateRows(response.json).toSeq)
      }
    }

    def find(hdr: TblHead[_], fieldValue: String): Future[Option[JsObject]] = {
      ws.url(endPoint).withQueryStringParameters(
        "filter" -> s"${hdr.symbol}=$fieldValue", "pageSize" -> "1"
      ).get().map { response =>
        Some(locateRows(response.json)(0))
      }
    }

    private def locateRows(js: JsValue) = (js \ responsePath).as[JsArray].value.map{_.as[JsObject]}
    private def locateTotalSize(js: JsValue) = (js \ totalSizePath).as[Int]
  }

}
