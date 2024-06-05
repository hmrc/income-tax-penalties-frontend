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

import play.api.libs.json.JsObject
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable.TblHead

import java.net.URLDecoder
import scala.language.postfixOps

object InternalTable {
  case class TblHead[T](
    name: String,
    lookup: JsObject => Option[T],
    markup: String => Html = Html(_),
    format: T => String = { value:T => value.toString }
  ) {
    val symbol: String = name.toLowerCase.replaceAll("[^a-z]+","-").stripSuffix("-")

    final def text(js: JsObject): Option[String] = lookup(js).map(format)

    final def html(js: JsObject): Html = markup(text(js).getOrElse(""))

    override def toString: String = name

    val comparator: (JsObject, JsObject)=>Int = { case (l: JsObject, r: JsObject) =>
      (lookup(l), lookup(r)) match {
        case (Some(lV: Comparable[T]), Some(rV: T)) => lV.compareTo(rV)
        case (Some(lV), Some(rV)) => lV.toString.compareTo(rV.toString) // fallback
        case (Some(_), None) => 1
        case (None, Some(_)) => -1
        case (None, None) => 0
      }
    }

    val matcher: (JsObject, String)=>Boolean = { case (js: JsObject, needle: String) =>
      lookup(js) match {
        case Some(v: T) => v.toString.contains(needle)
        case None => false
      }
    }
  }

  trait DataSource[T<:Product] {
    val table: InternalTable[T]

    case class Data[T<:Product](numPages: Int, pageNumber: Int, val rows: Seq[JsObject]) {
      lazy val html = rows.map{ table.format }
    }

    def pageData(filter: Seq[String], sort: Seq[String], page: Int, pageSize: Int = 20): Data[T]
    def find(hdr: TblHead[_], fieldValue: String): Option[JsObject]

    final def find(fieldName: String, fieldValue: String): Option[JsObject] = {
      table.headers.find(_.symbol == fieldName) match {
        case Some(hdr: TblHead[_]) =>
          find(hdr, fieldValue)
      }
    }
  }

  case class SimpleDataSource[T<:Product](table: InternalTable[T], val factory: () => Seq[JsObject]) extends DataSource[T] {
    def pageData(filter: Seq[String], sort: Seq[String], page: Int, pageSize: Int = 20): Data[T] = {
      val x: Seq[(JsObject,JsObject) => Int] = sort.map { spec =>
        spec.split("-", 2) match {
          case Array(direction, fieldName) =>
            val comparator = table.headers.find(_.symbol == fieldName).map(_.comparator)
            (direction, comparator) match {
              case ("asc", Some(comparator)) => { case (l,r) => comparator(l,r) }
              case ("desc", Some(comparator)) => { case (l,r) => -comparator(l,r) }
              case (_, None) => throw new Exception(s"""Bad column name: $fieldName""");
              case (dir, _) => throw new Exception(s"""Bad direction: $dir""");
            }
          case x => throw new Exception(s"""$spec became ${x.mkString(" ")}""");
        }
      }
      val startOpt: Option[Int] = None
      def xs(l: JsObject, r: JsObject): Boolean = x.foldLeft(startOpt){ case (a,f) => a.orElse(Some(f(l,r)).filterNot(_==0)) }.getOrElse(0) > 0

      val y: Seq[JsObject => Boolean] = filter.map { spec =>
        val decoded = URLDecoder.decode(spec, "UTF8")
        decoded.split("=", 2) match {
          case Array(fieldName, filterValue) =>
            val matcher = table.headers.find(_.symbol == fieldName).map{ case hdr => x: JsObject => hdr.matcher(x, filterValue) }
            matcher match {
              case Some(matcher) => matcher
              case None => throw new Exception(s"""Bad column name in filter spec: $decoded""");
            }
          case x => throw new Exception(s"""$spec becamse ${x.mkString(" ")}"""); { case i => true }
        }
      }
      def ys(js: JsObject): Boolean = y.foldLeft(true){ case (a,f) => a && f(js) }

      val rows = factory().map(_.as[JsObject]).filter(ys).sortWith(xs).grouped(pageSize).drop(page).nextOption().getOrElse(Seq.empty)

      Data(0, 0, rows)
    }

    def find(hdr: TblHead[_], fieldValue: String): Option[JsObject] = {
      factory().find { case js: JsObject =>
        val v: Option[String] = hdr.text(js)
        v.exists(_.contains(fieldValue))
      }
    }
  }
}

case class InternalTable[T<:Product](header: T) {
  val headers: Seq[TblHead[_]] = header.productIterator.toSeq.asInstanceOf[Seq[TblHead[Any]]]

  def format(js: JsObject): Seq[Html] = headers.map(_.html(js))
}