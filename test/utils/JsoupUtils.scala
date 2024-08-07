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

package utils

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object JsoupUtils {
  implicit class ElementsEx(val elements: Elements) {
    private val width = 160
    def dump(hint: String = ""): Elements = {
      println(if (hint.nonEmpty) "-- " + hint + "-".repeat(width-2-hint.length) else "-".repeat(width))
      println(elements.html)
      println("-".repeat(width))
      elements
    }
    def apply(i: Int): Element = elements.get(i)
  }
}
