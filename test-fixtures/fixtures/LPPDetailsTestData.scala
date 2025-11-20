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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp._

import java.time.LocalDate

trait LPPDetailsTestData {
  val principleChargeBillingStartDate: LocalDate = LocalDate.of(2021, 5, 1) //2021-05-01 All other dates based off this date
  val principleChargeBillingEndDate: LocalDate = principleChargeBillingStartDate.plusMonths(1) //2021-06-01
  val principleChargeBillingDueDate: LocalDate = principleChargeBillingEndDate.plusDays(6) //2021-06-07
  val penaltyChargeCreationDate: LocalDate = principleChargeBillingEndDate.plusDays(6) //2021-06-07
  val communicationDate: LocalDate = penaltyChargeCreationDate //2021-06-07
  val penaltyDueDate: LocalDate = penaltyChargeCreationDate.plusDays(31) //2021-07-08
  val lpp1PrincipleChargePaidDate: LocalDate = penaltyDueDate.plusDays(30) //2021-08-07
  val lpp2PrincipleChargePaidDate: LocalDate = penaltyDueDate.plusDays(45) //2021-08-22
  val timeToPayProposedOrAgreed: LocalDate = principleChargeBillingStartDate.plusMonths(1) //2021-07-01
  val principleChargeRef = "12345678901234"

  val sampleUnpaidLPP1Day15to30: LPPDetails = LPPDetails(
    principalChargeReference = principleChargeRef,
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = None,
    penaltyAmountPosted = 0,
    penaltyAmountAccruing = 1001.45,
    penaltyAmountOutstanding = Some(200),
    lpp1LRDays = Some("15"),
    lpp1HRDays = None,
    lpp2Days = None,
    lpp1LRCalculationAmt = Some(99.99),
    lpp1HRCalculationAmt = None,
    lpp2Percentage = None,
    lpp1LRPercentage = Some(2.00),
    lpp1HRPercentage = None,
    penaltyChargeCreationDate = Some(penaltyChargeCreationDate),
    communicationsDate = Some(communicationDate),
    penaltyChargeDueDate = Some(penaltyDueDate),
    appealInformation = None,
    principalChargeBillingFrom = principleChargeBillingStartDate,
    principalChargeBillingTo = principleChargeBillingEndDate,
    principalChargeDueDate = principleChargeBillingDueDate,
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    vatOutstandingAmount = Some(BigDecimal(123.45)),
    metadata = LPPDetailsMetadata(
      principalChargeMainTr = "4700",
      timeToPay = None
    )
  )

  val sampleUnpaidLPP1ProposedPaymentPlan: LPPDetails =
    sampleUnpaidLPP1Day15to30.copy(
      metadata = LPPDetailsMetadata(
        principalChargeMainTr = "4700",
        timeToPay = Some(TimeToPay(proposalDate = Some(timeToPayProposedOrAgreed), None))
      )
    )

  val sampleUnpaidLPP1AgreedPaymentPlan: LPPDetails =
    sampleUnpaidLPP1Day15to30.copy(
      metadata = LPPDetailsMetadata(
        principalChargeMainTr = "4700",
        timeToPay = Some(TimeToPay(None, agreementDate = Some(timeToPayProposedOrAgreed)))
      )
    )

  val sampleTaxPaidLPP1Day15to30: LPPDetails = sampleUnpaidLPP1Day15to30.copy(penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountAccruing = 0,
    penaltyAmountPosted = 1001.45,
    principalChargeLatestClearing = Some(LocalDate.now()))

  val samplePaidLPP1Day15to30: LPPDetails = sampleTaxPaidLPP1Day15to30.copy(penaltyAmountOutstanding = None)

  val sampleUnpaidLPP1: LPPDetails = LPPDetails(
    principalChargeReference = principleChargeRef,
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = None,
    penaltyAmountPosted = 0,
    penaltyAmountAccruing = 1001.45,
    penaltyAmountOutstanding = Some(200),
    lpp1LRDays = Some("15"),
    lpp1HRDays = Some("31"),
    lpp2Days = Some("31"),
    lpp1LRCalculationAmt = Some(99.99),
    lpp1HRCalculationAmt = Some(99.99),
    lpp2Percentage = Some(4.00),
    lpp1LRPercentage = Some(2.00),
    lpp1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
    penaltyChargeCreationDate = Some(penaltyChargeCreationDate),
    communicationsDate = Some(communicationDate),
    penaltyChargeDueDate = Some(penaltyDueDate),
    appealInformation = None,
    principalChargeBillingFrom = principleChargeBillingStartDate,
    principalChargeBillingTo = principleChargeBillingEndDate,
    principalChargeDueDate = principleChargeBillingDueDate,
    penaltyChargeReference = Some("PEN1234567"),
    principalChargeLatestClearing = None,
    vatOutstandingAmount = Some(BigDecimal(123.45)),
    metadata = LPPDetailsMetadata(
      principalChargeMainTr = "4700",
      principalChargeSubTr = None,
      principalChargeDocNumber = None,
      timeToPay = None
    )
  )

  val sampleUnpaidLPP1Day31 = sampleUnpaidLPP1
  val sampleTaxPaidLPP1Day31 = sampleUnpaidLPP1.copy(
    penaltyAmountAccruing = 0,
    penaltyAmountPosted = 1001.45,
    principalChargeLatestClearing = Some(LocalDate.now())
  )
  val samplePaidLPP1Day31 = sampleTaxPaidLPP1Day31.copy(penaltyAmountOutstanding = None)

  val samplePaidLPP1: LPPDetails = sampleUnpaidLPP1.copy(
    principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPosted = 1001.45,
    penaltyAmountPaid = Some(1001.45),
    penaltyAmountOutstanding = None,
    penaltyAmountAccruing = 0,
    appealInformation = None
  )

  val sampleLPP1AppealUnpaid: (AppealStatusEnum.Value, AppealLevelEnum.Value) => LPPDetails = (appealStatus, appealLevel) => sampleUnpaidLPP1.copy(
    appealInformation = Some(
      Seq(
        AppealInformationType(
          appealStatus = Some(appealStatus), appealLevel = Some(appealLevel)
        )
      )
    ),
    penaltyAmountPaid = Some(10),
    penaltyAmountOutstanding = Some(200),
    penaltyStatus = LPPPenaltyStatusEnum.Posted
  )

  val sampleLPP1AppealPaid: (AppealStatusEnum.Value, AppealLevelEnum.Value) => LPPDetails = (appealStatus, appealLevel) => sampleUnpaidLPP1.copy(
    appealInformation = Some(
      Seq(
        AppealInformationType(
          appealStatus = Some(appealStatus), appealLevel = Some(appealLevel)
        )
      )
    ),
    principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
    penaltyAmountOutstanding = None,
    penaltyAmountPaid = Some(1001.45),
    penaltyAmountPosted = 1001.45,
    penaltyAmountAccruing = 0,
    penaltyStatus = LPPPenaltyStatusEnum.Posted
  )

  val sampleLPP2: LPPDetails = sampleUnpaidLPP1.copy(
    penaltyCategory = LPPPenaltyCategoryEnum.LPP2
  )

  val sampleManualLPP: LPPDetails = LPPDetails(
    principalChargeReference = "09876543210987",
    penaltyCategory = LPPPenaltyCategoryEnum.MANUAL,
    penaltyChargeCreationDate = Some(penaltyChargeCreationDate),
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = None,
    penaltyAmountPosted = 999.99,
    penaltyAmountAccruing = 0,
    penaltyAmountOutstanding = Some(999.99),
    lpp1LRDays = None,
    lpp1HRDays = None,
    lpp2Days = None,
    lpp1LRCalculationAmt = None,
    lpp1HRCalculationAmt = None,
    lpp1LRPercentage = None,
    lpp1HRPercentage = None,
    lpp2Percentage = None,
    communicationsDate = None,
    penaltyChargeDueDate = None,
    appealInformation = None,
    principalChargeBillingFrom = principleChargeBillingStartDate,
    principalChargeBillingTo = principleChargeBillingEndDate,
    principalChargeDueDate = principleChargeBillingDueDate,
    penaltyChargeReference = None,
    principalChargeLatestClearing = None,
    vatOutstandingAmount = Some(BigDecimal(123.45)),
    metadata = LPPDetailsMetadata(
      principalChargeMainTr = "4787",
      None,
      None,
    )
  )
}
