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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils._


class LPPSummaryListRowHelper extends SummaryListRowHelper with DateFormatter {

  def penaltyTypeRow(penalty: LPPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("lpp.penaltyType.key"),
      value = Html(messages(s"lpp.penaltyType.${penalty.penaltyCategory}"))
    )

  def penaltyStatusRow(penalty: LPPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("lpp.penaltyType.key"),
      value = Html(messages(s"lpp.penaltyType.${penalty.penaltyCategory}"))
    )

  def addedOnRow(penalty: LPPDetails)(implicit messages: Messages): Option[SummaryListRow] =
    penalty.penaltyChargeCreationDate.map { creationDate =>
      summaryListRow(
        label = messages("lpp.addedOn.key"),
        value = Html(dateToString(creationDate))
      )
    }

  def incomeTaxPeriodRow(penalty: LPPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("lpp.incomeTaxPeriod.key"),
      value = Html(messages(
        "lpp.incomeTaxPeriod.value",
        penalty.principalChargeBillingFrom.getYear.toString,
        penalty.principalChargeBillingTo.getYear.toString
      ))
    )

  def incomeTaxDueRow(penalty: LPPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      label = messages("lpp.incomeTaxDue.key"),
      value = Html(dateToString(penalty.principalChargeDueDate))
    )

  def incomeTaxPaymentDateRow(penalty: LPPDetails)(implicit messages: Messages): SummaryListRow =
    summaryListRow(
      messages("lpp.incomeTaxPaymentDate.key"),
      Html(
        if (penalty.penaltyStatus.equals(LPPPenaltyStatusEnum.Posted) && penalty.principalChargeLatestClearing.isDefined) {
          dateToString(penalty.principalChargeLatestClearing.get)
        } else {
          messages("lpp.paymentNotReceived")
        }
      )
    )

  def payPenaltyByRow(penalty:LPPDetails)(implicit messages: Messages): SummaryListRow = summaryListRow(
    label = messages("lpp.incomeTaxDue.key"),
    value = Html(dateToString(penalty.principalChargeDueDate))
  )
}
