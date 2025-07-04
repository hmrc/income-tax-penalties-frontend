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

import play.api.i18n.Messages

import java.time.LocalDate

trait DateFormatter {

  private val htmlNonBroken: String => String = _.replace(" ", "\u00A0")

  def dateToString(date: LocalDate)(implicit messages: Messages): String =
    htmlNonBroken(s"${date.getDayOfMonth} ${messages(s"month.${date.getMonthValue}")} ${date.getYear}")

  def dateToYearString(date: LocalDate): String =
    htmlNonBroken(s"${date.getYear}")

  def dateToMonthYearString(date: LocalDate)(implicit messages: Messages): String =
    htmlNonBroken(s"${messages(s"month.${date.getMonthValue}")} ${date.getYear}")
}

object DateFormatter extends DateFormatter
