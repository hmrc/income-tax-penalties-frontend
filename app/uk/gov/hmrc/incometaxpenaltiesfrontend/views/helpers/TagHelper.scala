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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{CurrencyFormatter, TimeMachine}

import java.time.LocalDate

trait TagHelper {

  def getTagStatus(penalty: LSPDetails, isBreathingSpace: Boolean, threshold: Int, pointsRemovedAfterPoc: Option[Boolean] = None)(implicit messages: Messages, timeMachine: TimeMachine): Tag =
    penalty.penaltyStatus match {
      case LSPPenaltyStatusEnum.Inactive =>
        val isAppealStatusUpheld: Boolean = penalty.appealStatus.contains(AppealStatusEnum.Upheld)
        val isRemovedAfterPoc: Boolean = pointsRemovedAfterPoc.contains(true)
        val tagStatusMessage: String =
          (isAppealStatusUpheld, isRemovedAfterPoc) match {
            case (true, _) => messages("status.cancelled")
            case (_, true) => messages("status.removed")
            case _ => messages("status.expired")
          }
        Tag(Text(messages(tagStatusMessage)))
      case _ if penalty.outstandingAmount == 0 && isLsp4OrAdditional(penalty, threshold) => Tag(Text(messages("status.paid")), "govuk-tag--green")
      case _ if isBreathingSpace && isLsp4OrAdditional(penalty, threshold) => Tag(Text(messages("status.breathing.space")), "govuk-tag--yellow")
      case LSPPenaltyStatusEnum.Active if penalty.originalAmount > BigDecimal(0) =>
        showDueOrPartiallyPaidDueTag(penalty.outstandingAmount, penalty.amountPaid, penalty.chargeDueDate)
      case _ =>
        Tag(Text(messages("status.active")))
    }

  def isLsp4OrAdditional(penalty: LSPDetails, threshold: Int): Boolean = {
    if (penalty.penaltyOrder.exists(_.toInt >= threshold)) {
      if (penalty.penaltyStatus == LSPPenaltyStatusEnum.Inactive) {
        if (!penalty.appealStatus.contains(AppealStatusEnum.Upheld)) {
          return true
        }
      } else if (penalty.penaltyOrder.exists(_.toInt == threshold)) {
          return true
      }
    }
    false
  }

  def getTagStatus(penalty: LPPDetails, isBreathingSpace: Boolean)(implicit messages: Messages, timeMachine: TimeMachine): Tag =
    (penalty.appealStatus, penalty.penaltyStatus) match {
      case (Some(AppealStatusEnum.Upheld), _) => Tag(Text(messages("status.cancelled")))
      case (_, LPPPenaltyStatusEnum.Posted) if penalty.isPaid => Tag(Text(messages("status.paid")), "govuk-tag--green")
      case _ if isBreathingSpace => Tag(Text(messages("status.breathing.space")), "govuk-tag--yellow")
      case (_, LPPPenaltyStatusEnum.Accruing) => Tag(Text(messages("status.estimate")))
      case (_, _) => showDueOrPartiallyPaidDueTag(penalty.penaltyAmountOutstanding.getOrElse(0), penalty.penaltyAmountPaid.getOrElse(BigDecimal(0)), penalty.penaltyChargeDueDate)
    }

  private def showDueOrPartiallyPaidDueTag(penaltyAmountOutstanding: BigDecimal, penaltyAmountPaid: BigDecimal, chargeDueDate: Option[LocalDate] = None)(implicit messages: Messages, timeMachine: TimeMachine): Tag =
    (penaltyAmountOutstanding, penaltyAmountPaid, chargeDueDate) match {
      case (outstanding, _, _) if outstanding == 0 =>
        Tag(Text(messages("status.paid")), "govuk-tag--green")
      case (outstanding, paid, dueDate) if outstanding != 0 && paid > 0 && dueDate.exists(payBy => timeMachine.getCurrentDate().isAfter(payBy)) =>
        Tag(Text(messages("status.amountOverdue", CurrencyFormatter.parseBigDecimalTo2DecimalPlaces(outstanding))), "govuk-tag--red")
      case (outstanding, _, dueDate) if outstanding != 0 && dueDate.exists(payBy => timeMachine.getCurrentDate().isAfter(payBy)) =>
        Tag(Text(messages("status.overdue")), "govuk-tag--red")
      case (outstanding, paid, dueDate) if paid > 0 && dueDate.forall(payBy => !timeMachine.getCurrentDate().isAfter(payBy)) =>
        Tag(Text(messages("status.amountDue", CurrencyFormatter.parseBigDecimalTo2DecimalPlaces(outstanding))), "govuk-tag--red")
      case _ =>
        Tag(Text(messages("status.due")), "govuk-tag--red")
    }
}
