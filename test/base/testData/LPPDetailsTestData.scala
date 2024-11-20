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

package base.testData

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp._

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
  val timeToPayPeriodStart: LocalDate = principleChargeBillingStartDate.plusMonths(1) //2021-06-01
  val timeToPayPeriodEnd: LocalDate = timeToPayPeriodStart.plusMonths(1) //2021-07-01

  val sampleUnpaidLPP1: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = None,
    penaltyAmountPosted = 0,
    penaltyAmountAccruing = 1001.45,
    penaltyAmountOutstanding = None,
    LPP1LRDays = Some("15"),
    LPP1HRDays = Some("31"),
    LPP2Days = Some("31"),
    LPP1LRCalculationAmount = Some(99.99),
    LPP1HRCalculationAmount = Some(99.99),
    LPP2Percentage = Some(4.00),
    LPP1LRPercentage = Some(2.00),
    LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
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
      LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
      outstandingAmount = Some(99),
      timeToPay = None
    )
  )

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
    principalChargeLatestClearing = Some(lpp1PrincipleChargePaidDate),
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
    LPP1LRDays = None,
    LPP1HRDays = None,
    LPP2Days = None,
    LPP1LRCalculationAmount = None,
    LPP1HRCalculationAmount = None,
    LPP1LRPercentage = None,
    LPP1HRPercentage = None,
    LPP2Percentage = None,
    communicationsDate = None,
    penaltyChargeDueDate = None,
    appealInformation = None,
    principalChargeBillingFrom = principleChargeBillingStartDate,
    principalChargeBillingTo = principleChargeBillingEndDate,
    principalChargeDueDate = principleChargeBillingDueDate,
    penaltyChargeReference = None,
    principalChargeLatestClearing = None,
    vatOutstandingAmount = Some(BigDecimal(123.45)),
      LPPDetailsMetadata = LPPDetailsMetadata(
      mainTransaction = Some(MainTransactionEnum.ManualCharge),
      None,
      None,
    )
  )
}
