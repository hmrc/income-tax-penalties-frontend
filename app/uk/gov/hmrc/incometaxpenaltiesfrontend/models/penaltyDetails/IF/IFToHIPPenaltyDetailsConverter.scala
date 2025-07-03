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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.IF

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, JsValue, Json, Reads}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealInformationType
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.breathingSpace.BreathingSpace
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.{PenaltyDetails, Totalisations}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.JsonUtils

import java.time.LocalDate

object IFToHIPPenaltyDetailsConverter extends JsonUtils{

  lazy val totalisationReads: Reads[Totalisations] = (json: JsValue) =>
    for {
        lspTotalValue <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
        penalisedPrincipalTotal <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
        lppPostedTotal <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
        lppEstimatedTotal <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
        totalAccountOverdue <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
        totalAccountPostedInterest <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
        totalAccountAccruingInterest <- (json \ "LSPTotalValue").validateOpt[BigDecimal]
      } yield Totalisations(lspTotalValue, penalisedPrincipalTotal, lppPostedTotal,
        lppEstimatedTotal, totalAccountOverdue, totalAccountPostedInterest, totalAccountAccruingInterest
      )

  lazy val lspSummaryReads: Reads[LSPSummary] = (json: JsValue) => for {
    activePenaltyPoints <- (json \ "activePenaltyPoints").validate[Int]
    inactivePenaltyPoints <- (json \ "inactivePenaltyPoints").validate[Int]
    regimeThreshold <- (json \ "regimeThreshold").validate[Int]
    penaltyChargeAmount <- (json \ "penaltyChargeAmount").validate[BigDecimal]
    pocAchievementDate <- (json \ "PoCAchievementDate").validateOpt[LocalDate]
  } yield {
    LSPSummary(activePenaltyPoints, inactivePenaltyPoints, regimeThreshold, penaltyChargeAmount, pocAchievementDate)
  }

  lazy val lateSubmissionReads: Reads[LateSubmission] = (json: JsValue) => for {
    taxPeriodStartDate <- (json \ "taxPeriodStartDate").validateOpt[LocalDate]
    taxPeriodEndDate <- (json \ "taxPeriodEndDate").validateOpt[LocalDate]
    taxPeriodDueDate <- (json \ "taxPeriodDueDate").validateOpt[LocalDate]
    returnReceiptDate <- (json \ "returnReceiptDate").validateOpt[LocalDate]
    taxReturnStatus <- (json \ "taxReturnStatus").validateOpt[TaxReturnStatusEnum.Value]
  } yield {
    LateSubmission("lateSubmissionID", None, None, taxPeriodStartDate, taxPeriodEndDate, taxPeriodDueDate, returnReceiptDate, taxReturnStatus)
  }

  lazy val lspDetailsReads: Reads[LSPDetails] = (json: JsValue) =>
    for {
      penaltyNumber <- (json \ "penaltyNumber").validate[String]
      penaltyOrder <- (json \ "penaltyOrder").validateOpt[String]
      penaltyCategory <- (json \ "penaltyCategory")
        .validateOpt[LSPPenaltyCategoryEnum.Value]
      penaltyStatus <- (json \ "penaltyStatus")
        .validate[LSPPenaltyStatusEnum.Value]
      penaltyCreationDate <- (json \ "penaltyCreationDate").validate[LocalDate]
      penaltyExpiryDate <- (json \ "penaltyExpiryDate").validate[LocalDate]
      communicationsDate <- (json \ "communicationsDate").validateOpt[LocalDate]
      fapIndicator <- (json \ "fapIndicator").validateOpt[String]
      lateSubmissions <- (json \ "lateSubmissions")
        .validateOpt[Seq[LateSubmission]](Reads.seq[LateSubmission](lateSubmissionReads))
      expiryReason <- (json \ "expiryReason")
        .validateOpt[ExpiryReasonEnum.Value]
      appealInformation <- (json \ "appealInformation")
        .validateOpt[Seq[AppealInformationType]]
      chargeDueDate <- (json \ "chargeDueDate").validateOpt[LocalDate]
      chargeOutstandingAmount <- (json \ "chargeOutstandingAmount")
        .validateOpt[BigDecimal]
      chargeAmount <- (json \ "chargeAmount").validateOpt[BigDecimal]
    } yield {
      LSPDetails(
        penaltyNumber,
        penaltyOrder,
        penaltyCategory,
        penaltyStatus,
        penaltyCreationDate,
        penaltyExpiryDate,
        communicationsDate,
        fapIndicator,
        lateSubmissions,
        expiryReason,
        appealInformation,
        chargeDueDate,
        chargeOutstandingAmount,
        chargeAmount,
        None,
        None
      )
    }

  lazy val lateSubmissionPenaltyReads: Reads[LateSubmissionPenalty] = (
    (JsPath \ "summary").read[LSPSummary](lspSummaryReads) and
      (JsPath \ "details").read[Seq[LSPDetails]](Reads.seq[LSPDetails](lspDetailsReads))
    )(LateSubmissionPenalty.apply _)

  lazy val timeToPayReads: Reads[TimeToPay] = (
    (JsPath \ "TTPStartDate").readNullable[LocalDate] and
      (JsPath \ "TTPEndDate").readNullable[LocalDate]
    )(TimeToPay.apply _)

  lazy val lppMetadataReads: Reads[LPPDetailsMetadata] = (json: JsValue) =>
    for {
      principalChargeMainTr <- (json \ "mainTransaction").validateOpt[MainTransactionEnum.Value]
      timeToPay <-  (json \ "timeToPay").validateOpt[Seq[TimeToPay]](Reads.seq[TimeToPay] (timeToPayReads))
    } yield {
      LPPDetailsMetadata(
        principalChargeMainTr = principalChargeMainTr.getOrElse(MainTransactionEnum.ITSAReturnCharge),
        timeToPay = timeToPay
      )
    }

  lazy val lppDetailsReads: Reads[LPPDetails] = (json: JsValue) =>
    for {
      principalChargeReference <- (json \ "principalChargeReference").validate[String]
      penaltyCategory <- (json \ "penaltyCategory").validate[LPPPenaltyCategoryEnum.Value]
      penaltyStatus <- (json \ "penaltyStatus").validate[LPPPenaltyStatusEnum.Value]
      penaltyAmountAccruing <- (json \ "penaltyAmountAccruing").validate[BigDecimal]
      penaltyAmountPosted <- (json \ "penaltyAmountPosted").validate[BigDecimal]
      penaltyAmountPaid <- (json \ "penaltyAmountPaid").validateOpt[BigDecimal]
      penaltyAmountOutstanding <- (json \ "penaltyAmountOutstanding").validateOpt[BigDecimal]
      lpp1LRCalculationAmt <- (json \ "LPP1LRCalculationAmount").validateOpt[BigDecimal]
      lpp1LRDays <- (json \ "LPP1LRDays").validateOpt[String]
      lpp1LRPercentage <- (json \ "LPP1LRPercentage").validateOpt[BigDecimal]
      lpp1HRCalculationAmt <- (json \ "LPP1HRCalculationAmount").validateOpt[BigDecimal]
      lpp1HRDays <- (json \ "LPP1HRDays").validateOpt[String]
      lpp1HRPercentage <- (json \ "LPP1HRPercentage").validateOpt[BigDecimal]
      lpp2Days <- (json \ "LPP2Days").validateOpt[String]
      lpp2Percentage <- (json \ "LPP2Percentage").validateOpt[BigDecimal]
      penaltyChargeCreationDate <- (json \ "penaltyChargeCreationDate").validateOpt[LocalDate]
      communicationsDate <- (json \ "communicationsDate").validateOpt[LocalDate]
      penaltyChargeReference <- (json \ "penaltyChargeReference").validateOpt[String]
      penaltyChargeDueDate <- (json \ "penaltyChargeDueDate").validateOpt[LocalDate]
      appealInformation <- (json \ "appealInformation").validateOpt[Seq[AppealInformationType]]
      principalChargeBillingFrom <- (json \ "principalChargeBillingFrom").validate[LocalDate]
      principalChargeBillingTo <- (json \ "principalChargeBillingTo").validate[LocalDate]
      principalChargeDueDate <- (json \ "principalChargeDueDate").validate[LocalDate]
      principalChargeLatestClearing <- (json \ "principalChargeLatestClearing").validateOpt[LocalDate]
      vatOutstandingAmount <- (json \ "vatOutstandingAmount").validateOpt[BigDecimal]
      metadata <- Json.fromJson(json)(lppMetadataReads)
    } yield {
      LPPDetails(
        principalChargeReference = principalChargeReference,
        penaltyCategory = penaltyCategory,
        penaltyStatus = penaltyStatus,
        penaltyAmountAccruing = penaltyAmountAccruing,
        penaltyAmountPosted = penaltyAmountPosted,
        penaltyAmountPaid = penaltyAmountPaid,
        penaltyAmountOutstanding = penaltyAmountOutstanding,
        lpp1LRCalculationAmt = lpp1LRCalculationAmt,
        lpp1LRDays = lpp1LRDays,
        lpp1LRPercentage = lpp1LRPercentage,
        lpp1HRCalculationAmt = lpp1HRCalculationAmt,
        lpp1HRDays = lpp1HRDays,
        lpp1HRPercentage = lpp1HRPercentage,
        lpp2Days = lpp2Days,
        lpp2Percentage = lpp2Percentage,
        penaltyChargeCreationDate = penaltyChargeCreationDate,
        communicationsDate = communicationsDate,
        penaltyChargeReference = penaltyChargeReference,
        penaltyChargeDueDate = penaltyChargeDueDate,
        appealInformation = appealInformation,
        principalChargeBillingFrom = principalChargeBillingFrom,
        principalChargeBillingTo = principalChargeBillingTo,
        principalChargeDueDate = principalChargeDueDate,
        principalChargeLatestClearing = principalChargeLatestClearing,
        vatOutstandingAmount = vatOutstandingAmount,
        metadata = metadata
      )
    }

  lazy val latePaymentPenaltyReads: Reads[LatePaymentPenalty] = (json: JsValue) => {
    for {
      lppDetails <- (json \ "details").validate[Seq[LPPDetails]](Reads.seq[LPPDetails](lppDetailsReads))
    } yield {
      LatePaymentPenalty(lppDetails = Some(lppDetails))
    }
  }

  lazy val breathingSpaceReads: Reads[BreathingSpace] = (
    (JsPath \ "BSStartDate").read[LocalDate] and
      (JsPath \ "BSEndDate").read[LocalDate]
    )(BreathingSpace.apply _)

  lazy val penaltyDetailsReads: Reads[PenaltyDetails] = (json: JsValue) => {
    for {
      totalisations <- (json \ "totalisations").validateOpt[Totalisations](totalisationReads)
      lateSubmissionPenalty <- (json \ "lateSubmissionPenalty").validateOpt[LateSubmissionPenalty](lateSubmissionPenaltyReads)
      latePaymentPenalty <- (json \ "latePaymentPenalty").validateOpt[LatePaymentPenalty](latePaymentPenaltyReads)
      breathingSpace <- (json \ "breathingSpace").validateOpt[Seq[BreathingSpace]](Reads.seq[BreathingSpace](breathingSpaceReads))
    } yield {
      PenaltyDetails(totalisations,
        lateSubmissionPenalty,
        latePaymentPenalty,
        breathingSpace
      )
    }
  }

}
