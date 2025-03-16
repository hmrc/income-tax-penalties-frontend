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

  def dateToString(date: LocalDate)(implicit messages: Messages): String = {
    val dateNonBreaking = s"${date.getDayOfMonth} ${messages(s"month.${date.getMonthValue}")} ${date.getYear}"
    dateNonBreaking.replace(" ", "\u00A0")
  }

  def dateToMonthYearString(date: LocalDate)(implicit messages: Messages): String = {
    val dateNonBreaking = s"${messages(s"month.${date.getMonthValue}")} ${date.getYear}"
    dateNonBreaking.replace(" ", "\u00A0")
  }

  def dateNonBreakingSpaceMultiple(key: String, from: String, to: String)(implicit messages: Messages): String =
    messages(
      key,
      htmlNonBroken(from).format(from),
      htmlNonBroken(to).format(to)
    )

  def dateNonBreakingSpaceSingle(date: String): String = htmlNonBroken(date).format(date)

  def htmlNonBroken(string: String): String =
    string.replace(" ", "\u00A0")

}

object DateFormatter extends DateFormatter
