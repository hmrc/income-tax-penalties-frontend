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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils._

class LSPSummaryListRowHelper extends SummaryListRowHelper with DateFormatter {

  def taxPeriodSummaryRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =
    (penalty.taxPeriodStartDate, penalty.taxPeriodEndDate) match {
      case (Some(startDate), Some(endDate)) =>
        Some(summaryListRow(
          label = messages("lsp.quarter.key"),
          value = Html(messages("lsp.quarter.value", dateToString(startDate), dateToString(endDate)))
        ))
      case _ => None
    }

  def dueDateSummaryRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =
    penalty.dueDate.map { dueDate =>
      summaryListRow(
        label = messages("lsp.updateDue.key"),
        value = Html(dateToString(dueDate))
      )
    }

  def expiryReasonSummaryRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =
    penalty.expiryReason.map(expiryReason => {
      summaryListRow(
        label = messages("lsp.expiryReason.key"),
        value = Html(messages(s"expiryReason.${expiryReason.toString}"))
      )
    })

  def receivedDateSummaryRow(penalty: LSPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("lsp.updateSubmitted.key"),
      value = Html(
        penalty.receiptDate.fold(messages("lsp.updateSubmitted.notReceived"))(dateToString(_).replace(" ", "\u00A0"))
      )
    )

  def pointExpiryDate(penalty: LSPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      messages("lsp.expiry.key"),
      Html(dateToMonthYearString(penalty.penaltyExpiryDate))
    )
}
