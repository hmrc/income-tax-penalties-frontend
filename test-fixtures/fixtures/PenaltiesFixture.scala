/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.{PenaltyDetails, Totalisations}

import java.time.LocalDate

trait PenaltiesFixture {

  val lateSubmissionId: String = "testSubId"
  val taxPeriod = "2021-2022"
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
      principalChargeMainTr = MainTransactionEnum.ITSAReturnCharge,
      timeToPay = None
    )
  )

  val taxPeriodStart: LocalDate = LocalDate.of(2021, 1, 6)
  val taxPeriodEnd: LocalDate = LocalDate.of(2021, 2, 5)
  val taxPeriodDue: LocalDate = taxPeriodEnd.plusMonths(1).plusDays(7)
  val receiptDate: LocalDate = taxPeriodDue.plusDays(7)
  val creationDate: LocalDate = LocalDate.of(2021, 3, 7)
  val expiryDate: LocalDate = creationDate.plusYears(2)
  val chargeDueDate: LocalDate = creationDate.plusMonths(1)

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


  val samplePenaltyDetailsModel: PenaltyDetails = PenaltyDetails(
    totalisations = Some(Totalisations(
      lspTotalValue = Some(200),
      penalisedPrincipalTotal = Some(2000),
      lppPostedTotal = Some(165.25),
      lppEstimatedTotal = Some(15.26),
      totalAccountOverdue = None,
      totalAccountPostedInterest = None,
      totalAccountAccruingInterest = None
    )),
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1, inactivePenaltyPoints = 0, regimeThreshold = 4, penaltyChargeAmount = 200, pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(sampleLateSubmissionPoint))),
    latePaymentPenalty = Some(
      LatePaymentPenalty(
        Some(Seq(
          sampleUnpaidLPP1.copy(metadata = LPPDetailsMetadata(principalChargeMainTr = MainTransactionEnum.ITSAReturnFirstLPP, timeToPay = None))
        ))
      )
    ),
    breathingSpace = None
  )

  val samplePenaltyDetailsModelWithoutMetadata: PenaltyDetails = samplePenaltyDetailsModel.copy(latePaymentPenalty = Some(LatePaymentPenalty(Some(Seq(sampleUnpaidLPP1)))))


  val testMtdItId: String = "1234567890"
  val testNino: String = "AA123456A"
  val testArn: String = "XARN1234567890"
  val testPoCAchievementDate: LocalDate = LocalDate.of(2024, 12, 1)
  val testFromDate: LocalDate = testPoCAchievementDate.minusYears(2)

  val sampleDate1: LocalDate = LocalDate.of(2021, 1, 1)
  val sampleDate2: LocalDate = LocalDate.of(2021, 2, 1)
  val sampleDate3: LocalDate = LocalDate.of(2021, 3, 1)
  val sampleDate4: LocalDate = LocalDate.of(2021, 4, 1)

  val sampleLSP: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = Some("01"),
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    fapIndicator = None,
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2069-10-30"),
    expiryReason = None,
    communicationsDate = Some(LocalDate.parse("2069-10-30")),
    lateSubmissions = Some(Seq(
      LateSubmission(
        lateSubmissionID = lateSubmissionId,
        incomeSource = Some("Income source"),
        taxPeriod = Some(taxPeriod),
        taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
        returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
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

  val sampleLPPPosted: LPPDetails = LPPDetails(
    principalChargeReference = "12345678901234",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(277.00),
    penaltyAmountOutstanding = Some(123.00),
    penaltyAmountPosted = 400,
    penaltyAmountAccruing = 0,
    lpp1LRDays = Some("15"),
    lpp1HRDays = Some("31"),
    lpp2Days = Some("31"),
    lpp1LRCalculationAmt = Some(10000.00),
    lpp1HRCalculationAmt = Some(10000.00),
    lpp2Percentage = Some(4.00),
    lpp1LRPercentage = Some(2.00),
    lpp1HRPercentage = Some(2.00),
    penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
    communicationsDate = Some(LocalDate.parse("2069-10-30")),
    penaltyChargeDueDate = Some(LocalDate.parse("2021-03-08")),
    appealInformation = Some(Seq(AppealInformationType(
      appealStatus = Some(AppealStatusEnum.Unappealable),
      appealLevel = Some(AppealLevelEnum.FirstStageAppeal)
    ))),
    principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
    principalChargeBillingTo = LocalDate.parse("2021-02-01"),
    principalChargeDueDate = LocalDate.parse("2021-03-07"),
    penaltyChargeReference = Some("1234567890"),
    principalChargeLatestClearing = Some(LocalDate.parse("2069-10-30")),
    vatOutstandingAmount = Some(BigDecimal(123.45)),
    metadata = LPPDetailsMetadata(
      principalChargeMainTr = MainTransactionEnum.ITSAReturnCharge,
      timeToPay = None
    )
  )

  val sampleTotalisations = Totalisations(
    lspTotalValue = Some(BigDecimal(200)),
    penalisedPrincipalTotal = Some(BigDecimal(2000)),
    lppPostedTotal = Some(BigDecimal(165.25)),
    lppEstimatedTotal = Some(BigDecimal(15.26)),
    totalAccountOverdue = Some(10432.21),
    totalAccountPostedInterest = Some(4.32),
    totalAccountAccruingInterest = Some(1.23)
  )

  val samplePenaltyDetails: PenaltyDetails = PenaltyDetails(
    totalisations = Some(sampleTotalisations),
    lateSubmissionPenalty =
      Some(LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 1,
          inactivePenaltyPoints = 0,
          regimeThreshold = 4,
          penaltyChargeAmount = 0,
          pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
        ),
        details = Seq(sampleLSP)
      )),
    latePaymentPenalty = Some(LatePaymentPenalty(Some(Seq(sampleLPPPosted)))),
    breathingSpace = None
  )

  val getPenaltyDetailsPayloadWithAddedPoint = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1,
        regimeThreshold = 4,
        inactivePenaltyPoints = 0,
        penaltyChargeAmount = 0,
        pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(
        sampleLSP.copy(
          penaltyNumber = "1234567890",
          penaltyOrder = Some("01"),
          fapIndicator = Some("X"),
          penaltyExpiryDate = LocalDate.of(2023, 2, 1),
          penaltyCreationDate = LocalDate.of(2021, 1, 1)
        )
      )
    )
    ),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val getPenaltyDetailsPayloadWithOverThreshold = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 3,
        regimeThreshold = 2,
        inactivePenaltyPoints = 0,
        penaltyChargeAmount = 200.00,
        pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(
        sampleLSP.copy(
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyNumber = "1234567890",
          penaltyOrder = Some("03"),
          chargeAmount = Some(200.00),
          chargeOutstandingAmount = Some(200.00),
          chargeDueDate = Some(sampleDate1),
          lateSubmissions = Some(Seq(
            LateSubmission(
              lateSubmissionID = lateSubmissionId,
              incomeSource = Some("Income source"),
              taxPeriod = Some(taxPeriod),
              taxPeriodStartDate = Some(LocalDate.parse("2021-01-01")),
              taxPeriodEndDate = Some(LocalDate.parse("2021-01-31")),
              taxPeriodDueDate = Some(LocalDate.parse("2021-03-07")),
              returnReceiptDate = Some(LocalDate.parse("2021-03-25")),
              taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
            )
          ))
        ),
        sampleLSP.copy(
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold),
          penaltyNumber = "1234567891",
          penaltyOrder = Some("02"),
          chargeAmount = Some(200.00),
          chargeOutstandingAmount = Some(200.00),
          chargeDueDate = Some(sampleDate1)
        ),
        sampleLSP.copy(
          penaltyNumber = "1234567892",
          penaltyOrder = Some("01")
        )
      )
    )
    ),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val getPenaltyDetailsPayloadWithRemovedPoints = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1,
        regimeThreshold = 4,
        inactivePenaltyPoints = 1,
        penaltyChargeAmount = 0,
        pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          penaltyNumber = "1234567891",
          penaltyOrder = Some("02"),
          fapIndicator = Some("X"),
          expiryReason = Some(ExpiryReasonEnum.Adjustment),
          lateSubmissions = Some(Seq(
            LateSubmission(
              lateSubmissionID = lateSubmissionId,
              incomeSource = Some("Income source"),
              taxPeriod = Some(taxPeriod),
              taxPeriodStartDate = Some(LocalDate.parse("2021-01-01")),
              taxPeriodEndDate = Some(LocalDate.parse("2021-01-31")),
              taxPeriodDueDate = Some(LocalDate.parse("2021-03-07")),
              returnReceiptDate = Some(LocalDate.parse("2021-03-25")),
              taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
            )
          ))
        ),
        sampleLSP
      )
    )),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val getPenaltiesDataPayloadWith2PointsAndOneRemovedPoint: PenaltyDetails = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 2,
        regimeThreshold = 4,
        inactivePenaltyPoints = 1,
        penaltyChargeAmount = 0,
        pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyNumber = "1234567893",
          penaltyOrder = Some("03")
        ),
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyNumber = "1234567892",
          penaltyOrder = Some("02")
        ),
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          penaltyNumber = "1234567891",
          penaltyOrder = Some("01"),
          fapIndicator = Some("X"),
          expiryReason = Some(ExpiryReasonEnum.Adjustment)
        )
      ))
    ),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val getPenaltiesDataPayloadOutOfOrder: PenaltyDetails = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 3,
        regimeThreshold = 4,
        inactivePenaltyPoints = 0,
        penaltyChargeAmount = 0,
        pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyNumber = "1234567893",
          penaltyOrder = Some("01"),
          lateSubmissions = Some(Seq(LateSubmission(
            lateSubmissionID = lateSubmissionId,
            incomeSource = Some("Income source"),
            taxPeriod = Some(taxPeriod),
            taxPeriodStartDate = Some(sampleDate1.minusMonths(3)),
            taxPeriodEndDate = Some(sampleDate1.minusMonths(3).plusDays(30)),
            taxPeriodDueDate = Some(sampleDate1),
            returnReceiptDate = Some(sampleDate1),
            taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)))
          )
        ),
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyNumber = "1234567892",
          penaltyOrder = Some("02"),
          lateSubmissions = Some(Seq(LateSubmission(
            lateSubmissionID = lateSubmissionId,
            incomeSource = Some("Income source"),
            taxPeriod = Some(taxPeriod),
            taxPeriodStartDate = Some(sampleDate1.minusMonths(2)),
            taxPeriodEndDate = Some(sampleDate1.minusMonths(2).plusDays(29)),
            taxPeriodDueDate = Some(sampleDate1),
            returnReceiptDate = Some(sampleDate1),
            taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)))
          )
        ),
        sampleLSP.copy(
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          penaltyNumber = "1234567893",
          penaltyOrder = Some("03"),
          lateSubmissions = Some(Seq(LateSubmission(
            lateSubmissionID = lateSubmissionId,
            incomeSource = Some("Income source"),
            taxPeriod = Some(taxPeriod),
            taxPeriodStartDate = Some(sampleDate1.minusMonths(1)),
            taxPeriodEndDate = Some(sampleDate1.minusMonths(1).plusDays(30)),
            taxPeriodDueDate = Some(sampleDate1),
            returnReceiptDate = Some(sampleDate1),
            taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)))
          )
        )
      ))
    ),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val paidLatePaymentPenalty: LatePaymentPenalty = LatePaymentPenalty(
    lppDetails = Some(Seq(sampleLPPPosted.copy(
      penaltyAmountPaid = Some(BigDecimal(400)),
      penaltyAmountOutstanding = Some(BigDecimal(0)),
      penaltyAmountPosted = 400,
      penaltyAmountAccruing = 0,
      principalChargeBillingFrom = sampleDate1,
      principalChargeBillingTo = sampleDate1.plusDays(30),
      principalChargeDueDate = sampleDate1.plusMonths(2).plusDays(6),
      principalChargeLatestClearing = Some(sampleDate1.plusMonths(2).plusDays(7))
    ))))

  val latePaymentPenaltyWithAdditionalPenalty: LatePaymentPenalty = LatePaymentPenalty(lppDetails = Some(
    Seq(
    sampleLPPPosted.copy(
      penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(BigDecimal(123.45)),
      penaltyAmountOutstanding = Some(BigDecimal(0)),
      penaltyAmountPosted = 123.45,
      penaltyAmountAccruing = 0,
      principalChargeBillingFrom = sampleDate1,
      principalChargeBillingTo = sampleDate1.plusDays(30),
      principalChargeDueDate = sampleDate1.plusMonths(2).plusDays(6),
      principalChargeLatestClearing = Some(sampleDate1.plusMonths(2).plusDays(7))
    ),
    sampleLPPPosted.copy(
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(BigDecimal(200)),
      penaltyAmountOutstanding = Some(BigDecimal(200)),
      penaltyAmountPosted = 400,
      penaltyAmountAccruing = 0,
      principalChargeBillingFrom = sampleDate1,
      principalChargeBillingTo = sampleDate1.plusMonths(1),
      principalChargeDueDate = sampleDate1.plusMonths(2).plusDays(6),
      principalChargeLatestClearing = Some(sampleDate1.plusMonths(2).plusDays(7))
    ))))

  val latePaymentPenaltyVATUnpaid: LatePaymentPenalty = LatePaymentPenalty(
    lppDetails = Some(Seq(
      sampleLPPPosted.copy(
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyAmountPaid = Some(BigDecimal(200)),
        penaltyAmountOutstanding = Some(BigDecimal(200)),
        penaltyAmountPosted = 400,
        penaltyAmountAccruing = 0,
        principalChargeBillingFrom = sampleDate1,
        principalChargeBillingTo = sampleDate1.plusDays(30),
        principalChargeDueDate = sampleDate1.plusMonths(2).plusDays(6),
        principalChargeLatestClearing = None
      )
    ))
  )

  val latePaymentPenaltyWithAppeal = Some(LatePaymentPenalty(Some(Seq(
    sampleLPPPosted.copy(
      penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(BigDecimal(400)),
      penaltyAmountOutstanding = Some(BigDecimal(0)),
      penaltyAmountPosted = 400,
      penaltyAmountAccruing = 0,
      principalChargeBillingFrom = sampleDate1,
      principalChargeBillingTo = sampleDate1.plusDays(30),
      principalChargeDueDate = sampleDate1.plusMonths(2).plusDays(6),
      principalChargeLatestClearing = Some(sampleDate1.plusMonths(2).plusDays(7)),
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Under_Appeal),
        appealLevel = Some(AppealLevelEnum.FirstStageAppeal))))
    ))
  )))

  val getPenaltiesDataPayloadWithPaidLPP: PenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    latePaymentPenalty = Some(paidLatePaymentPenalty),
  )

  val getPenaltiesDataPayloadWithLPPAndAdditionalPenalty: PenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    latePaymentPenalty = Some(latePaymentPenaltyWithAdditionalPenalty)
  )

  val getPenaltiesDataPayloadWithLPPVATUnpaid: PenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    latePaymentPenalty = Some(latePaymentPenaltyVATUnpaid)
  )

  val getPenaltyDetailsPayloadWithLPPVATUnpaidAndVATOverviewAndLSPsDue: PenaltyDetails = getPenaltyDetailsPayloadWithAddedPoint.copy(
    totalisations = Some(Totalisations(
      lspTotalValue = Some(400),
      penalisedPrincipalTotal = Some(121.40),
      lppPostedTotal = Some(165.25),
      lppEstimatedTotal = Some(46.55),
      totalAccountOverdue = Some(10432.21),
      totalAccountPostedInterest = Some(4.32),
      totalAccountAccruingInterest = Some(1.23)
    )),
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 3,
        regimeThreshold = 2,
        inactivePenaltyPoints = 0,
        penaltyChargeAmount = 0,
        pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(
        sampleLSP.copy(
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Charge),
          penaltyNumber = "1234567892",
          penaltyOrder = Some("03"),
          chargeAmount = Some(200.00),
          chargeOutstandingAmount = Some(200.00),
          chargeDueDate = Some(sampleDate1)
        ),
        sampleLSP.copy(
          penaltyCategory = Some(LSPPenaltyCategoryEnum.Threshold),
          penaltyNumber = "1234567891",
          penaltyOrder = Some("02"),
          chargeAmount = Some(200.00),
          chargeOutstandingAmount = Some(200.00),
          chargeDueDate = Some(sampleDate1)
        ),
        sampleLSP.copy(
          penaltyNumber = "1234567890",
          penaltyOrder = Some("01")
        )
      )
    )
    ),
    latePaymentPenalty = Some(latePaymentPenaltyVATUnpaid)
  )

  val getPenaltyPayloadWithLPPAppeal: PenaltyDetails = getPenaltiesDataPayloadWithPaidLPP.copy(
    latePaymentPenalty = latePaymentPenaltyWithAppeal
  )

  val unpaidLatePaymentPenalty: LatePaymentPenalty = LatePaymentPenalty(
    lppDetails = Some(Seq(
      sampleLPPPosted.copy(
        penaltyAmountPaid = Some(BigDecimal(0)),
        penaltyAmountOutstanding = Some(BigDecimal(400)),
        penaltyAmountPosted = 400,
        penaltyAmountAccruing = 0,
        principalChargeLatestClearing = None
      )
    )))

  val getPenaltiesDetailsPayloadWithMultiplePenaltyPeriodInLSP: PenaltyDetails = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 1,
          inactivePenaltyPoints = 0,
          regimeThreshold = 4,
          penaltyChargeAmount = 200,
          pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
        ),
        details = Seq(
          sampleLSP.copy(
            penaltyNumber = "1234567890",
            penaltyOrder = Some("01"),
            lateSubmissions = Some(Seq(
              LateSubmission(
                lateSubmissionID = lateSubmissionId,
                incomeSource = Some("Income source 2"),
                taxPeriod = Some(taxPeriod),
                taxPeriodStartDate = Some(sampleDate1),
                taxPeriodEndDate = Some(sampleDate1.plusDays(14)),
                taxPeriodDueDate = Some(sampleDate1.plusMonths(4).plusDays(7)),
                returnReceiptDate = Some(sampleDate1.plusMonths(4).plusDays(12)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              ),
              LateSubmission(
                lateSubmissionID = lateSubmissionId,
                incomeSource = Some("Income source"),
                taxPeriod = Some(taxPeriod),
                taxPeriodStartDate = Some(sampleDate1.plusDays(16)),
                taxPeriodEndDate = Some(sampleDate1.plusDays(31)),
                taxPeriodDueDate = Some(sampleDate1.plusMonths(4).plusDays(23)),
                returnReceiptDate = Some(sampleDate1.plusMonths(4).plusDays(25)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              )
            ))
          )
        )
      )
    ),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val getPenaltiesDetailsPayloadWithExpiredPoints: PenaltyDetails = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = Some(
      LateSubmissionPenalty(
        summary = LSPSummary(
          activePenaltyPoints = 2,
          inactivePenaltyPoints = 1,
          regimeThreshold = 4,
          penaltyChargeAmount = 200,
          pocAchievementDate = Some(LocalDate.of(2022, 1, 1))
        ),
        details = Seq(
          sampleLSP.copy(
            penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
            penaltyNumber = "1234567892",
            penaltyOrder = Some("03"),
            lateSubmissions = Some(Seq(
              LateSubmission(
                lateSubmissionID = lateSubmissionId,
                incomeSource = Some("Income source"),
                taxPeriod = Some(taxPeriod),
                taxPeriodStartDate = Some(sampleDate2),
                taxPeriodEndDate = Some(sampleDate2.plusDays(27)),
                taxPeriodDueDate = Some(sampleDate2.plusMonths(2).plusDays(7)),
                returnReceiptDate = Some(sampleDate2.plusMonths(2).plusDays(12)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              )
            ))
          ),
          sampleLSP.copy(
            penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
            penaltyStatus = LSPPenaltyStatusEnum.Inactive,
            expiryReason = Some(ExpiryReasonEnum.Reversal),
            penaltyNumber = "1234567891",
            penaltyOrder = Some("02")
          ),
          sampleLSP.copy(
            penaltyNumber = "1234567890",
            penaltyOrder = Some("01"),
            lateSubmissions = Some(Seq(
              LateSubmission(
                lateSubmissionID = lateSubmissionId,
                incomeSource = Some("Income source"),
                taxPeriod = Some(taxPeriod),
                taxPeriodStartDate = Some(sampleDate1),
                taxPeriodEndDate = Some(sampleDate1.plusDays(30)),
                taxPeriodDueDate = Some(sampleDate1.plusMonths(2).plusDays(7)),
                returnReceiptDate = Some(sampleDate1.plusMonths(2).plusDays(12)),
                taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
              )
            ))
          )
        )
      )
    ),
    latePaymentPenalty = None,
    breathingSpace = None
  )

  val getPenaltyDataPayloadWithManualLPP = PenaltyDetails(
    totalisations = None,
    lateSubmissionPenalty = None,
    latePaymentPenalty = Some(
      LatePaymentPenalty(
        lppDetails = Some(Seq(LPPDetails(
          principalChargeReference = "1234567890",
          penaltyCategory = LPPPenaltyCategoryEnum.MANUAL,
          penaltyChargeCreationDate = Some(LocalDate.parse("2069-10-30")),
          penaltyStatus = LPPPenaltyStatusEnum.Posted,
          penaltyAmountPaid = None,
          penaltyAmountPosted = 100.00,
          penaltyAmountAccruing = 0,
          penaltyAmountOutstanding = Some(100.00),
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
          principalChargeBillingFrom = LocalDate.parse("2021-01-01"),
          principalChargeBillingTo = LocalDate.parse("2021-02-01"),
          principalChargeDueDate = LocalDate.parse("2021-03-07"),
          penaltyChargeReference = None,
          principalChargeLatestClearing = None,
          vatOutstandingAmount = None,
          metadata = LPPDetailsMetadata(
            principalChargeMainTr = MainTransactionEnum.ManualCharge,
            timeToPay = None
          )
        ))
        )
      )
    ),
    breathingSpace = None
  )

}
