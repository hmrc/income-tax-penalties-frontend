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

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{PenaltyDetails, Totalisations}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.{FirstLatePaymentPenaltyCalculationData, LLPCharge, SecondLatePaymentPenaltyCalculationData}

import java.time.LocalDate

trait PenaltiesDetailsTestData extends LSPDetailsTestData with LPPDetailsTestData {

  val sampleDate: LocalDate = LocalDate.of(2021, 1, 1)

  val emptyPenaltyDetailsModel: PenaltyDetails = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = None,
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val lateSubmissionPenalty: LateSubmissionPenalty = LateSubmissionPenalty(
    summary = LSPSummary(
      activePenaltyPoints = 1,
      inactivePenaltyPoints = 0,
      regimeThreshold = 4,
      penaltyChargeAmount = 200,
      PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
    ),
    details = Seq(sampleLateSubmissionPoint)
  )

  val latePaymentPenalty: LatePaymentPenalty = LatePaymentPenalty(Seq(
    sampleUnpaidLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnFirstLPP), outstandingAmount = Some(20), timeToPay = None))
  ))

  def sampleFirstLPPCalcData(is15to30Days: Boolean = true,
                             isPenaltyPaid: Boolean = false,
                             isIncomeTaxPaid: Boolean = false,
                             isEstimate: Boolean = true,
                             isOverdue: Boolean = false) = {
    val penaltyChargeDueDate = if(isOverdue) LocalDate.now().minusDays(5) else LocalDate.now().plusDays(5)

    FirstLatePaymentPenaltyCalculationData(
      penaltyAmount = 1001.45,
      taxPeriodStartDate = penaltyChargeDueDate.minusDays(90),
      taxPeriodEndDate = penaltyChargeDueDate.minusDays(60),
      isPenaltyPaid = isPenaltyPaid,
      incomeTaxIsPaid = isIncomeTaxPaid,
      isEstimate = isEstimate,
      isPenaltyOverdue = isOverdue,
      payPenaltyBy = penaltyChargeDueDate,
      penaltyChargeReference = if(is15to30Days && !isIncomeTaxPaid) None else Some("PEN1234567"),
      llpLRCharge = LLPCharge(
        99.99, "15", 2.00
      ),
      llpHRCharge = if(!is15to30Days) {Some(
        LLPCharge(
          99.99, "31", 2.00
        )
      )} else None
    )
  }

  def sampleSecondLPPCalcData(isPenaltyPaid: Boolean = false,
                              isIncomeTaxPaid: Boolean = false,
                              isEstimate: Boolean = true,
                              isOverdue: Boolean = false) = {
    val penaltyChargeDueDate = if(isOverdue) LocalDate.now().minusDays(5) else LocalDate.now().plusDays(5)

    SecondLatePaymentPenaltyCalculationData(
      penaltyAmount = 1001.45,
      taxPeriodStartDate = penaltyChargeDueDate.minusDays(90),
      taxPeriodEndDate = penaltyChargeDueDate.minusDays(60),
      isPenaltyPaid = isPenaltyPaid,
      incomeTaxIsPaid = isIncomeTaxPaid,
      isEstimate = isEstimate,
      isPenaltyOverdue = isOverdue,
      payPenaltyBy = penaltyChargeDueDate,
      penaltyChargeReference = if(!isIncomeTaxPaid) None else Some("PEN1234567"),
      penaltyPercentage = 2.00,
      daysOverdue = "4",
      amountPenaltyAppliedTo =  20.00,
      chargeStartDate = LocalDate.now(),
      chargeEndDate = LocalDate.now()
    )
  }

  def getPenaltyDetailsForCalculationPage(firstLPPCalData: FirstLatePaymentPenaltyCalculationData): PenaltyDetails = {
    val lppDetails = LPPDetails(
      principalChargeReference = principleChargeRef,
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyStatus = if(firstLPPCalData.isEstimate) LPPPenaltyStatusEnum.Accruing else LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = if(firstLPPCalData.isPenaltyPaid) Some(firstLPPCalData.penaltyAmount) else None,
      penaltyAmountPosted = if(firstLPPCalData.isEstimate) 0 else firstLPPCalData.penaltyAmount,
      penaltyAmountAccruing = if(firstLPPCalData.isEstimate) firstLPPCalData.penaltyAmount else 0,
      penaltyAmountOutstanding = if(firstLPPCalData.isPenaltyPaid) Some(0) else Some(firstLPPCalData.penaltyAmount),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("31"),
      LPP2Days = Some("31"),
      LPP1LRCalculationAmount = Some(99.99),
      LPP1HRCalculationAmount = if(firstLPPCalData.llpHRCharge.isDefined) Some(99.99) else None,
      LPP2Percentage = None,
      LPP1LRPercentage = Some(2.00),
      LPP1HRPercentage = if(firstLPPCalData.llpHRCharge.isDefined) Some(BigDecimal(2.00).setScale(2)) else None,
      penaltyChargeCreationDate = Some(firstLPPCalData.payPenaltyBy.minusDays(30)),
      communicationsDate = Some(firstLPPCalData.payPenaltyBy),
      penaltyChargeDueDate = Some(firstLPPCalData.payPenaltyBy),
      appealInformation = None,
      principalChargeBillingFrom = firstLPPCalData.taxPeriodStartDate,
      principalChargeBillingTo = firstLPPCalData.taxPeriodEndDate,
      principalChargeDueDate = firstLPPCalData.payPenaltyBy,
      penaltyChargeReference = Some("PEN1234567"),
      principalChargeLatestClearing = if(firstLPPCalData.incomeTaxIsPaid) Some(firstLPPCalData.payPenaltyBy) else None,
      vatOutstandingAmount = None,
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None
      )
    )
    val lpp = LatePaymentPenalty(Seq(lppDetails
      .copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnFirstLPP), outstandingAmount = Some(20), timeToPay = None))))
    PenaltyDetails(
      totalisations = Some(Totalisations(
        LSPTotalValue = Some(200),
        penalisedPrincipalTotal = Some(2000),
        LPPPostedTotal = Some(165.25),
        LPPEstimatedTotal = Some(15.26),
        totalAccountOverdue = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None
      )),
      lateSubmissionPenalty = Some(lateSubmissionPenalty),
      latePaymentPenalty = Some(lpp),
      breathingSpace = None
    )
  }

  def getPenaltyDetailsForSecondCalculationPage(secondLPPCalData: SecondLatePaymentPenaltyCalculationData): PenaltyDetails = {
    val lppDetails = LPPDetails(
      principalChargeReference = principleChargeRef,
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyStatus = if(secondLPPCalData.isEstimate) LPPPenaltyStatusEnum.Accruing else LPPPenaltyStatusEnum.Posted,
      penaltyAmountPaid = if(secondLPPCalData.isPenaltyPaid) Some(secondLPPCalData.penaltyAmount) else None,
      penaltyAmountPosted = if(secondLPPCalData.isEstimate) 0 else secondLPPCalData.penaltyAmount,
      penaltyAmountAccruing = if(secondLPPCalData.isEstimate) secondLPPCalData.penaltyAmount else 0,
      penaltyAmountOutstanding = if(secondLPPCalData.isPenaltyPaid) Some(0) else Some(secondLPPCalData.penaltyAmount),
      LPP1LRDays = Some("15"),
      LPP1HRDays = Some("31"),
      LPP2Days = Some("31"),
      LPP1LRCalculationAmount = Some(99.99),
      LPP1HRCalculationAmount = Some(99.99),
      LPP2Percentage = None,
      LPP1LRPercentage = Some(2.00),
      LPP1HRPercentage = Some(BigDecimal(2.00).setScale(2)),
      penaltyChargeCreationDate = Some(secondLPPCalData.payPenaltyBy.minusDays(30)),
      communicationsDate = Some(secondLPPCalData.payPenaltyBy),
      penaltyChargeDueDate = Some(secondLPPCalData.payPenaltyBy),
      appealInformation = None,
      principalChargeBillingFrom = secondLPPCalData.taxPeriodStartDate,
      principalChargeBillingTo = secondLPPCalData.taxPeriodEndDate,
      principalChargeDueDate = secondLPPCalData.payPenaltyBy,
      penaltyChargeReference = Some("PEN1234567"),
      principalChargeLatestClearing = if(secondLPPCalData.incomeTaxIsPaid) Some(secondLPPCalData.payPenaltyBy) else None,
      vatOutstandingAmount = None,
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
        outstandingAmount = Some(99),
        timeToPay = None
      )
    )
    val lpp = LatePaymentPenalty(Seq(lppDetails
      .copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnFirstLPP), outstandingAmount = Some(20), timeToPay = None))))
    PenaltyDetails(
      totalisations = Some(Totalisations(
        LSPTotalValue = Some(200),
        penalisedPrincipalTotal = Some(2000),
        LPPPostedTotal = Some(165.25),
        LPPEstimatedTotal = Some(15.26),
        totalAccountOverdue = None,
        totalAccountPostedInterest = None,
        totalAccountAccruingInterest = None
      )),
      lateSubmissionPenalty = Some(lateSubmissionPenalty),
      latePaymentPenalty = Some(lpp),
      breathingSpace = None
    )
  }

  val latePaymentPenalty2: LatePaymentPenalty = LatePaymentPenalty(Seq(
    sampleLPP2.copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnSecondLPP), outstandingAmount = Some(20), timeToPay = None))
  ))

  val samplePenaltyDetailsModel: PenaltyDetails = PenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = Some(lateSubmissionPenalty),
    latePaymentPenalty = Some(latePaymentPenalty),
    breathingSpace = None
  )

  val samplePenaltyDetailsLPP2Model: PenaltyDetails = PenaltyDetails(
    totalisations = Some(Totalisations(
      LSPTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      LPPPostedTotal = Some(165.25),
      LPPEstimatedTotal = Some(15.26),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = Some(lateSubmissionPenalty),
    latePaymentPenalty = Some(latePaymentPenalty2),
    breathingSpace = None
  )

  val samplePenaltyDetailsModelWithoutMetadata: PenaltyDetails = samplePenaltyDetailsModel.copy(latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleUnpaidLPP1))))
}