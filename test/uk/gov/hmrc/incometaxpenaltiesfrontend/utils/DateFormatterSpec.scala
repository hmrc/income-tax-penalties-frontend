/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.utils

import fixtures.messages.MonthMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}

import java.time.LocalDate

class DateFormatterSpec extends AnyWordSpec with Matchers with DateFormatter with GuiceOneAppPerSuite {

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "DateFormatter" when {

    Seq(MonthMessages.English, MonthMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"rendering in the language '${messagesForLanguage.lang.name}'" should {

        "calling .dateToString()" should {

          "format to d MMMMM yyyy" in {
            val dateNonBreaking = s"1 ${messagesForLanguage.january} 2025".replace(" ", "\u00A0")
            DateFormatter.dateToString(LocalDate.of(2025, 1, 1)) shouldBe dateNonBreaking
          }
        }

        "calling .dateToMonthYearString()" should {

          "format to MMMMM yyyy" in {
            val dateNonBreaking = s"${messagesForLanguage.january} 2025".replace(" ", "\u00A0")
            DateFormatter.dateToMonthYearString(LocalDate.of(2025, 1, 1)) shouldBe dateNonBreaking
          }
        }
      }
    }
  }
}
