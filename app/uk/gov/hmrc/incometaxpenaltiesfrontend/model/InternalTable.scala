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

package uk.gov.hmrc.incometaxpenaltiesfrontend.model

import play.api.libs.json.{JsObject, JsValue}
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable.TblHead

import java.util.Comparator
import scala.language.postfixOps

object InternalTable {
  case class TblHead[T](
    name: String,
    lookup: JsObject => Option[T],
    format: T => String = { value:T => value.toString }
  ) {
    val symbol = name.toLowerCase.replaceAll("[^a-z]+","-").stripSuffix("-")

    final def html(js: JsObject): String = lookup(js).map(format).getOrElse("")

    override def toString: String = name

    val comparator: (JsObject, JsObject)=>Int = { case (l: JsObject, r: JsObject) =>
      (lookup(l), lookup(r)) match {
//        case (Some(lV: Comparable[T]), Some(rV: Comparable[T])) =>
//          println(s"""########## $lV $rV  ############ """)
//          lV.compareTo(rV)
        case (Some(lV: T), Some(rV: T)) => lV.asInstanceOf[Comparable[T]].compareTo(rV)
        case (Some(_), None) => 1
        case (None, Some(_)) => -1
        case (None, None) => 0
      }
    }
  }
}

case class InternalTable[T<:Product](header: T) {
  val headers: Seq[TblHead[_]] = header.productIterator.toSeq.asInstanceOf[Seq[TblHead[Any]]]

}
