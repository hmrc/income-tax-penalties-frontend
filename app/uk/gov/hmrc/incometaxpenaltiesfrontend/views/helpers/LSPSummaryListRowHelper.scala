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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.{LSPDetails, LSPTypeEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils._

import java.time.MonthDay

class LSPSummaryListRowHelper extends SummaryListRowHelper with DateFormatter {

  def missingOrLateIncomeSourcesSummaryRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] = {
    if (penalty.dueDate.exists(d => MonthDay.from(d) != MonthDay.of(1, 31))) {
      Some(summaryListRow(
        label = messages("lsp.missingOrLateIncomeSources.key"),
        value = Html(
          """<ul class="govuk-list govuk-list--bullet">
            |  <li>Missing Income Sources</li>
            |  <li>Late Income Sources</li>
            |</ul>""".stripMargin
        )
      ))
    } else None
  }


  def pointExpiredOnRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =
    Some(summaryListRow(
      label = messages("lsp.pointExpiredOn.key"),
      value = Html(messages(dateToString(penalty.penaltyExpiryDate)))
    ))

  def penaltyStatusRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =
    Some(summaryListRow(
      label = messages("lpp.penaltyType.key"),
      value = Html(messages(s"lpp.penaltyType.${penalty.penaltyCategory}"))
    ))


  def taxPeriodSummaryRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =

    (penalty.taxPeriodStartDate, penalty.taxPeriodEndDate) match {
      case (Some(startDate), Some(endDate)) =>
        if(penalty.dueDate.exists(d => MonthDay.from(d) != MonthDay.of(1, 31))) {
          Some (summaryListRow (
            label = messages("lsp.updatePeriod.key"),
            value = Html(messages("lsp.updatePeriod.value", dateToString (startDate), dateToString (endDate) ) )
          ))
        } else None
      case _ => None
    }

  def payPenaltyByRow(penalty: LSPDetails, threshold: Int)(implicit messages: Messages): Option[SummaryListRow] = {
    if(penalty.penaltyOrder.exists(_.toInt >= threshold) && !penalty.appealStatus.contains(AppealStatusEnum.Upheld) && penalty.lspTypeEnum != LSPTypeEnum.RemovedPoint) {
      penalty.chargeDueDate.map { chargeDueDate =>
       summaryListRow(
          label = messages("lsp.pay.penalty.by"),
          value = Html(dateToString(chargeDueDate))
        )
      }
    } else None
  }

  def taxYearSummaryRow(penalty: LSPDetails)(implicit messages: Messages): Option[SummaryListRow] =

    (penalty.taxPeriodStartDate, penalty.taxPeriodEndDate) match {
      case (Some(startDate), Some(endDate)) =>
        if(penalty.dueDate.exists(d => MonthDay.from(d) == MonthDay.of(1, 31))) {
          Some (summaryListRow (
            label = messages ("lsp.updateYear.key"),
            value = Html (messages ("lsp.updateYear.value", dateToYearString (startDate), dateToYearString (endDate)))
          ))
        } else None
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
      Html(dateToString(penalty.penaltyExpiryDate))
    )
}
