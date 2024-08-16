/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import play.api.i18n.Messages
import utils.DisplayFormats._

import java.time.{LocalDate, LocalDateTime}

class DisplayFormatsSpec extends SpecBase {

  private val application = applicationBuilder().build()
  implicit val messages: Messages = messages(application)

  "DisplayFormats" - {
    "return a formatted LocalDate" - {
      "dateToDayMonthYearString is called" in {
        Some(LocalDate.of(2021, 1, 1)).toDayMonthYear mustBe "1\u00A0January\u00A02021"
      }

      "dateTimeToDayMonthYearString is called" in {
        Some(LocalDateTime.of(2021, 1, 1, 1, 1, 1)).toDayMonthYear mustBe "1\u00A0January\u00A02021"
      }

      "dateToMonthYearString is called" in {
        Some(LocalDate.of(2021, 1, 1)).toMonthYear mustBe "January\u00A02021"
      }

      "dateTimeToMonthYearString is called" in {
        Some(LocalDateTime.of(2021, 1, 1, 1, 1, 1)).toMonthYear mustBe "January\u00A02021"
      }

      "dateToYearString is called" in {
        Some(LocalDate.of(2021, 1, 1)).toYear mustBe "2021"
      }

      "dateTimeToYearString is called" in {
        Some(LocalDateTime.of(2021, 1, 1, 1, 1, 1)).toYear mustBe "2021"
      }
    }

    "return an empty string" - {
      "dateToDayMonthYearString is called" in {
        Option.empty[LocalDate].toDayMonthYear mustBe ""
      }

      "dateTimeToDayMonthYearString is called" in {
        Option.empty[LocalDate].toMonthYear mustBe ""
      }

      "dateTimeToMonthYearString is called" in {
        Option.empty[LocalDateTime].toDayMonthYear mustBe ""
      }

      "dateToMonthYearString is called" in {
        Option.empty[LocalDateTime].toMonthYear mustBe ""
      }

      "dateTimeToYearString is called" in {
        Option.empty[LocalDate].toYear mustBe ""
      }

      "dateToYearString is called" in {
        Option.empty[LocalDateTime].toYear mustBe ""
      }
    }

  }
//    "return the formatted Welsh LocalDate" in {
//      "dateToString is called" in {
//        dateToString(LocalDate.of(2021, 1, 1))(cyMessages) shouldBe "1\u00A0Ionawr\u00A02021"
//      }
//
//      "dateTimeToString is called" in {
//        dateTimeToString(LocalDateTime.of(2021, 1, 1, 1, 1, 1))(cyMessages) shouldBe "1\u00A0Ionawr\u00A02021"
//      }
//
//      "dateTimeToMonthYearString is called" in {
//        dateTimeToMonthYearString(LocalDateTime.of(2021, 1, 1, 1, 1, 1))(cyMessages) shouldBe "Ionawr\u00A02021"
//      }
//
//      "dateToMonthYearString is called" in {
//        dateToMonthYearString(LocalDate.of(2021, 1, 1))(cyMessages) shouldBe "Ionawr\u00A02021"
//      }
//    }
//  }
}
