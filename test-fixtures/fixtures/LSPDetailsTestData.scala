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

package fixtures

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp._

import java.time.LocalDate

trait LSPDetailsTestData {
  val lateSubmissionId: String = "testSubId"
  val taxPeriod = "2021-2022"
  val taxPeriodStart: LocalDate = LocalDate.of(2021, 1, 6)
  val taxPeriodEnd: LocalDate = LocalDate.of(2021, 2, 5)
  val taxPeriodDue: LocalDate = taxPeriodEnd.plusMonths(1).plusDays(7)
  val receiptDate: LocalDate = taxPeriodDue.plusDays(7)
  val creationDate: LocalDate = LocalDate.of(2021, 3, 7)
  val expiryDate: LocalDate = creationDate.plusYears(2)
  val chargeDueDate: LocalDate = creationDate.plusMonths(1)

  lazy val lateSubmission = LateSubmission(
    lateSubmissionID = lateSubmissionId,
    incomeSource = Some("Income source"),
    taxPeriod = Some(taxPeriod),
    taxPeriodStartDate = Some(taxPeriodStart),
    taxPeriodEndDate = Some(taxPeriodEnd),
    taxPeriodDueDate = Some(taxPeriodDue),
    returnReceiptDate = Some(receiptDate),
    taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
  )

  val sampleLateSubmissionPoint: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = Some("01"),
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    fapIndicator = None,
    penaltyCreationDate = creationDate,
    penaltyExpiryDate = expiryDate,
    expiryReason = None,
    communicationsDate = Some(creationDate),
    lateSubmissions = Some(Seq(
      LateSubmission(
        lateSubmissionID = lateSubmissionId,
        incomeSource = Some("Income source"),
        taxPeriod = Some(taxPeriod),
        taxPeriodStartDate = Some(taxPeriodStart),
        taxPeriodEndDate = Some(taxPeriodEnd),
        taxPeriodDueDate = Some(taxPeriodDue),
        returnReceiptDate = Some(receiptDate),
        taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
      )
    )),
    appealInformation = None,
    chargeAmount = None,
    chargeOutstandingAmount = None,
    chargeDueDate = None,
    triggeringProcess = None,
    chargeReference = None
  )

  val sampleLateSubmissionPenaltyCharge: LSPDetails = sampleLateSubmissionPoint.copy(
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
    penaltyOrder = Some("01"),
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(chargeDueDate)
  )

  val sampleAdditionalLateSubmissionPenaltyCharge: LSPDetails = sampleLateSubmissionPoint.copy(
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    fapIndicator = Some("X")
  )

  val sampleLateSubmissionPenaltyChargeWithMultiplePeriods: LSPDetails = sampleLateSubmissionPenaltyCharge.copy(
    lateSubmissions = Some(Seq(
      LateSubmission(
        lateSubmissionID = lateSubmissionId,
        incomeSource = Some("Income source"),
        taxPeriod = Some(taxPeriod),
        taxPeriodStartDate = Some(taxPeriodStart),
        taxPeriodEndDate = Some(taxPeriodEnd),
        taxPeriodDueDate = Some(taxPeriodDue),
        returnReceiptDate = Some(receiptDate),
        taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
      ),
      LateSubmission(
        lateSubmissionID = lateSubmissionId,
        incomeSource = Some("Income source"),
        taxPeriod = Some(taxPeriod),
        taxPeriodStartDate = Some(taxPeriodStart.plusMonths(1)),
        taxPeriodEndDate = Some(taxPeriodEnd.plusMonths(1)),
        taxPeriodDueDate = Some(taxPeriodDue.plusMonths(1)),
        returnReceiptDate = Some(receiptDate.plusMonths(1)),
        taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
      )
    ))
  )

  val sampleLateSubmissionPointReturnSubmitted: LSPDetails = sampleLateSubmissionPoint.copy(
    lateSubmissions = Some(Seq(
      LateSubmission(
        lateSubmissionID = lateSubmissionId,
        incomeSource = Some("Income source"),
        taxPeriod = Some(taxPeriod),
        taxPeriodStartDate = Some(taxPeriodStart),
        taxPeriodEndDate = Some(taxPeriodEnd),
        taxPeriodDueDate = Some(taxPeriodDue),
        returnReceiptDate = Some(receiptDate),
        taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
      )
    ))
  )

  val sampleLateSubmissionPointReturnWithNoPenaltyCategory: LSPDetails = sampleLateSubmissionPoint.copy(
    penaltyNumber = "0987654321",
    penaltyCategory = None
  )

  val sampleRemovedPenaltyPoint: LSPDetails = sampleLateSubmissionPoint.copy(
    penaltyStatus = LSPPenaltyStatusEnum.Inactive,
    penaltyOrder = None,
    fapIndicator = Some("X"),
    expiryReason = Some(ExpiryReasonEnum.Adjustment)
  )


  val sampleRemovedPenaltyPointWithPocAchieved: LSPDetails = sampleLateSubmissionPoint.copy(
    penaltyStatus = LSPPenaltyStatusEnum.Inactive,
    penaltyOrder = None,
    fapIndicator = None,
    expiryReason = Some(ExpiryReasonEnum.Compliance)
  )

  val sampleExpiredPenaltyPoint: LSPDetails = sampleLateSubmissionPoint.copy(
    penaltyStatus = LSPPenaltyStatusEnum.Inactive,
    penaltyOrder = None,
    expiryReason = Some(ExpiryReasonEnum.NaturalExpiration)
  )

  val samplePenaltyPointNotSubmitted: LSPDetails = sampleLateSubmissionPoint.copy(
    lateSubmissions = Some(
      Seq(
        LateSubmission(
          lateSubmissionID = lateSubmissionId,
          incomeSource = Some("Income source"),
          taxPeriod = Some(taxPeriod),
          taxPeriodStartDate = Some(taxPeriodStart),
          taxPeriodEndDate = Some(taxPeriodEnd),
          taxPeriodDueDate = Some(taxPeriodDue),
          returnReceiptDate = None,
          taxReturnStatus = Some(TaxReturnStatusEnum.Open)
        )
      )
    )
  )

  val samplePenaltyPointAppeal: (AppealStatusEnum.Value, AppealLevelEnum.Value) => LSPDetails = (appealStatus, appealLevel) => sampleLateSubmissionPoint.copy(
    penaltyStatus = if (appealStatus.equals(AppealStatusEnum.Upheld)) LSPPenaltyStatusEnum.Inactive else LSPPenaltyStatusEnum.Active,
    appealInformation = Some(
      Seq(
        AppealInformationType(
          appealStatus = Some(appealStatus), appealLevel = Some(appealLevel)
        )
      )
    )
  )
}
