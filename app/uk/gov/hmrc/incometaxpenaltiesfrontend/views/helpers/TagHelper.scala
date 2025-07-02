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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.{LPPDetails, LPPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.{LSPDetails, LSPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.CurrencyFormatter

trait TagHelper {

  def getTagStatus(penalty: LSPDetails)(implicit messages: Messages): Tag =
    penalty.penaltyStatus match {
      case LSPPenaltyStatusEnum.Inactive =>
        Tag(Text(messages(
          if (penalty.appealStatus.contains(AppealStatusEnum.Upheld)) messages("status.upheld") else messages("status.expired")
        )))
      case LSPPenaltyStatusEnum.Active if penalty.originalAmount > BigDecimal(0) =>
        showDueOrPartiallyPaidDueTag(penalty.outstandingAmount, penalty.amountPaid)
      case _ =>
        Tag(Text(messages("status.active")))
    }

  def getTagStatus(penalty: LPPDetails)(implicit messages: Messages): Tag =
    (penalty.appealStatus, penalty.penaltyStatus) match {
      case (Some(AppealStatusEnum.Upheld), _) => Tag(Text(messages("status.upheld")))
      case (_, LPPPenaltyStatusEnum.Accruing) => Tag(Text(messages("status.estimate")))
      case (_, LPPPenaltyStatusEnum.Posted) if penalty.isPaid => Tag(Text(messages("status.paid")), "govuk-tag--green")
      case (_, _) => showDueOrPartiallyPaidDueTag(penalty.penaltyAmountOutstanding.getOrElse(0), penalty.penaltyAmountPaid.getOrElse(BigDecimal(0)))
    }

  private def showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding: BigDecimal, penaltyAmountPaid: BigDecimal)(implicit messages: Messages): Tag =
    (penaltyAmountOutstanding, penaltyAmountPaid) match {
      case (outstanding, _) if outstanding == 0 =>
        Tag(Text(messages("status.paid")), "govuk-tag--green")
      case (outstanding, paid) if paid > 0 =>
        Tag(Text(messages("status.amountDue", CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(outstanding))), "govuk-tag--red")
      case _ =>
        Tag(Text(messages("status.due")), "govuk-tag--red")
    }
}
