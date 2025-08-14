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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PenaltyPeriodHelper

import java.time.LocalDate

case class LSPDetails(penaltyNumber: String,
                      penaltyOrder: Option[String],
                      penaltyCategory: Option[LSPPenaltyCategoryEnum.Value],
                      penaltyStatus: LSPPenaltyStatusEnum.Value,
                      penaltyCreationDate: LocalDate,
                      penaltyExpiryDate: LocalDate,
                      communicationsDate: Option[LocalDate],
                      fapIndicator: Option[String],
                      lateSubmissions: Option[Seq[LateSubmission]],
                      expiryReason: Option[ExpiryReasonEnum.Value],
                      appealInformation: Option[Seq[AppealInformationType]],
                      chargeDueDate: Option[LocalDate],
                      chargeOutstandingAmount: Option[BigDecimal],
                      chargeAmount: Option[BigDecimal],
                      triggeringProcess: Option[String],
                      chargeReference: Option[String]) {

  private val AppealInfoWithHighestAppealLevel: Option[AppealInformationType] = appealInformation.flatMap(_.maxByOption(_.appealLevel.map(_.id).getOrElse(-1)))
  val appealLevel: Option[AppealLevelEnum.Value] = AppealInfoWithHighestAppealLevel.flatMap(_.appealLevel)
  val appealStatus: Option[AppealStatusEnum.Value] = AppealInfoWithHighestAppealLevel.flatMap(_.appealStatus)

  val lspTypeEnum: LSPTypeEnum.Value =
    (penaltyCategory, appealStatus) match {
      case (Some(LSPPenaltyCategoryEnum.Threshold), _) => LSPTypeEnum.Financial
      case (Some(LSPPenaltyCategoryEnum.Charge), _) => LSPTypeEnum.Financial
      case (_, Some(AppealStatusEnum.Upheld)) if penaltyStatus == LSPPenaltyStatusEnum.Inactive => LSPTypeEnum.AppealedPoint
      case (_, _) if fapIndicator.contains("X") => if (penaltyStatus == LSPPenaltyStatusEnum.Active) LSPTypeEnum.AddedFAP else LSPTypeEnum.RemovedFAP
      case (_, _) if penaltyStatus == LSPPenaltyStatusEnum.Inactive && expiryReason.isDefined => LSPTypeEnum.RemovedPoint
      case _ => LSPTypeEnum.Point
    }

  val sortedLateSubmission: Option[LateSubmission] =
    lateSubmissions.map(PenaltyPeriodHelper.sortedPenaltyPeriod).flatMap(_.headOption)

  val isReturnSubmitted: Boolean =
    sortedLateSubmission.exists(_.taxReturnStatus.contains(TaxReturnStatusEnum.Fulfilled))

  val taxPeriodStartDate: Option[LocalDate] = sortedLateSubmission.flatMap(_.taxPeriodStartDate)
  val taxPeriodEndDate: Option[LocalDate] = sortedLateSubmission.flatMap(_.taxPeriodEndDate)
  val dueDate: Option[LocalDate] = sortedLateSubmission.flatMap(_.taxPeriodDueDate)
  val receiptDate: Option[LocalDate] = sortedLateSubmission.flatMap(_.returnReceiptDate)

  val isFAP: Boolean = expiryReason.exists(_.equals(ExpiryReasonEnum.Adjustment))

  val originalAmount: BigDecimal = chargeAmount.getOrElse(BigDecimal(0))
  val outstandingAmount: BigDecimal = chargeOutstandingAmount.getOrElse(BigDecimal(0))
  val amountPaid: BigDecimal = originalAmount - outstandingAmount
}

object LSPDetails {
  implicit val format: OFormat[LSPDetails] = Json.format[LSPDetails]
}
