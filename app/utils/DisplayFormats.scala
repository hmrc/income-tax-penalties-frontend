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

import play.api.i18n.Messages

import java.time.{LocalDate, LocalDateTime}

object DisplayFormats {

  implicit class LocalDateEx(date: LocalDate)(implicit messages: Messages) {
    implicit def displayDayMonthYear: String =
      s"${date.getDayOfMonth}\u00A0${messages(s"month.${date.getMonthValue}")}\u00A0${date.getYear}"

    implicit def displayMonthYear: String =
      s"${messages(s"month.${date.getMonthValue}")}\u00A0${date.getYear}"
  }

  implicit class LocalDateTimeEx(dateTime: LocalDateTime)(implicit messages: Messages) {
    implicit def displayDayMonthYear: String =
      s"${dateTime.getDayOfMonth}\u00A0${messages(s"month.${dateTime.getMonthValue}")}\u00A0${dateTime.getYear}"

    implicit def displayMonthYear: String =
      s"${messages(s"month.${dateTime.getMonthValue}")}\u00A0${dateTime.getYear}"
  }
}
