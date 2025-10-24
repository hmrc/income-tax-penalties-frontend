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

package fixtures.messages

import play.api.i18n
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter.dateToString

import java.time.LocalDate

object ComplianceTimelineMessages {

  sealed trait Messages {
    def taxReturn(fromDate: LocalDate, toDate: LocalDate)(implicit messages: i18n.Messages): String =
      s"Tax return: ${dateToString(fromDate)} to ${dateToString(toDate)}"

    def updatePeriod(fromDate: LocalDate, toDate: LocalDate)(implicit messages: i18n.Messages): String =
      s"Update period: ${dateToString(fromDate)} to ${dateToString(toDate)}"

    def dueDate(dueDate: LocalDate, isLate: Boolean)(implicit messages: i18n.Messages): String =
      if(isLate) {
        s"Due on ${dateToString(dueDate)}. Send this missing submission now."
      } else {
        s"Send by ${dateToString(dueDate)}"
      }
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override def taxReturn(fromDate: LocalDate, toDate: LocalDate)(implicit messages: i18n.Messages): String =
      s"Ffurflen Dreth: ${dateToString(fromDate)} i ${dateToString(toDate)}"

    override def updatePeriod(fromDate: LocalDate, toDate: LocalDate)(implicit messages: i18n.Messages): String =
      s"Cyfnod diweddaru: ${dateToString(fromDate)} i ${dateToString(toDate)}"

    override def dueDate(dueDate: LocalDate, isLate: Boolean)(implicit messages: i18n.Messages): String =
      if(isLate) {
        s"Y dyddiad cau yw ${dateToString(dueDate)}. Anfonwch y cyflwyniad sydd ar goll nawr."
      } else {
        s"Anfonwch hwn erbyn ${dateToString(dueDate)}"
      }
  }
}
