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

package models

import connectors.PenaltiesConnector
import connectors.PenaltiesConnector.{GetPenaltyDetails, LPPDetails, LSPDetails, TaxReturnStatusEnum, getPenaltyDetailsFmt}
import connectors.PenaltiesConnector.{GetPenaltyDetails, LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LSPDetails, TaxReturnStatusEnum}
import play.api.i18n.Messages
import utils.DisplayFormats.{LocalDateEx, displayDayMonthYear, displayMonthYear}
import java.time.LocalDate
import scala.annotation.unused

class Penalties(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages) {

  private val lspSummary = penaltyDetails.lateSubmissionPenalty.map(_.summary)

  val totalLSPPs: Int = lspSummary.map(_.activePenaltyPoints).getOrElse(0)

  def regimeLSPThreshold: Int = lspSummary.map(_.regimeThreshold).get

  private val lspDetails: Option[LSPDetails]  = penaltyDetails.lateSubmissionPenalty.map(_.details.head)
  val penaltyRemovedDate: Either[String, Option[LocalDate]] = lspDetails match
    case Some(value) => Right(value.lateSubmissions.head.last.taxPeriodDueDate)
    case None => Left("No LSP details available.")

  val penaltyRemoveDateToString: String = penaltyRemovedDate match {
    case Right(Some(date)) =>
      val year = date.getYear
      val month = date.getMonthValue
      if (month == 12) messages("month.1") + " " + (year + 2).toString
      else messages(s"month.${month + 1}") + " " + (year + 1).toString
    case Right(None) => "Error extracting the date."
    case Left(_) => "No date found within the empty LSP details class."
  }

  class LateSubmissionPenalty(lspDetails: LSPDetails) {
    private val headSubmission = lspDetails.lateSubmissions.head.last
    val penaltyNumber: String = lspDetails.penaltyNumber
    val ordinal: String = lspDetails.penaltyOrder.map(_.toInt.toString).get // "1"
    val status: String = lspDetails.penaltyStatus.toString // "ACTIVE"
    val source: String = lspDetails.incomeSourceName.getOrElse("") // "JB Painting and Decorating"
    val quarterFrom: String = headSubmission.taxPeriodStartDate.toDayMonthYear // "6 April 2027"
    val quarterTo: String = headSubmission.taxPeriodEndDate.toDayMonthYear //"5 July 2027"
    val updateDue: String = headSubmission.taxPeriodDueDate.toDayMonthYear //"5 August 2027"
    val updateSubmitted: String = headSubmission.taxReturnStatus match {
      case Some(TaxReturnStatusEnum.Fulfilled) =>
        headSubmission.returnReceiptDate.map(displayDayMonthYear).getOrElse("Not yet received")
      case _ =>
        "Not yet received"
    }
    val dueToExpire: String = displayMonthYear(lspDetails.penaltyExpiryDate); // "September 2029"

    val annualPenalty: Boolean = quarterFrom.contains("6\u00A0April") && quarterTo.contains("5\u00A0April") && (quarterTo.takeRight(4).toInt == quarterFrom.takeRight(4).toInt + 1)

    val taxYearFrom: String = headSubmission.taxPeriodStartDate.toYear // 2026

    val taxYearTo: String = headSubmission.taxPeriodEndDate.toYear // 2027
  }

  val lateSubmissionPenalties: Seq[LateSubmissionPenalty] =
    penaltyDetails.lateSubmissionPenalty.map(_.details).getOrElse(Seq()).map(new LateSubmissionPenalty(_)).sortBy(_.ordinal.toInt).reverse

  class LatePaymentPenalty(lppDetails: LPPDetails) {
    val penaltyType: PenaltiesConnector.LPPPenaltyCategoryEnum.Value = lppDetails.penaltyCategory // LPP1 or LPP2
    val penaltyTypeString: String = lppDetails.penaltyCategory match {
      case LPPPenaltyCategoryEnum.LPP1 => "First penalty for late payment"
      case LPPPenaltyCategoryEnum.LPP2 => "Second penalty for late payment"
    }
    val dueDate: String = displayDayMonthYear(lppDetails.principalChargeDueDate) // "31 January 2028"
    val latestClearing: String = lppDetails.principalChargeLatestClearing.toDayMonthYear // "19 February 2028" or None
    val amountAccruing: BigDecimal = lppDetails.penaltyAmountAccruing // 400.00
    val amountPosted: BigDecimal = lppDetails.penaltyAmountPosted // 350.00
    val totalAmount: BigDecimal = (amountAccruing + amountPosted).setScale(2, BigDecimal.RoundingMode.HALF_UP)
    val isPaid: Boolean = lppDetails.penaltyAmountPaid match {
      case Some(amount) => (lppDetails.penaltyAmountPosted != 0) && (lppDetails.penaltyAmountPosted - amount == 0)
      case None => false
    }
    val status: String = lppDetails.penaltyStatus match {
      case LPPPenaltyStatusEnum.Accruing => if(lppDetails.penaltyAmountOutstanding == Some(0) || lppDetails.penaltyAmountOutstanding.isEmpty) "Estimate" else "Due"
      case LPPPenaltyStatusEnum.Posted => if(isPaid) {"Paid"} else {"Posted"}
    } // Active or Inactive
    val taxYearFrom: String = Some(lppDetails.principalChargeBillingFrom).toYear
    val taxYearTo: String = Some(lppDetails.principalChargeBillingTo).toYear
  }

  val latePaymentPenalties: Seq[LatePaymentPenalty] =
    penaltyDetails.latePaymentPenalty.map(_.details).getOrElse(Seq()).map(new LatePaymentPenalty(_))

  val accountHasLPPs: Boolean = penaltyDetails.latePaymentPenalty.nonEmpty
  
  val LPPSeqSize: Int = latePaymentPenalties.size
  
  val allPenaltiesPaid: Seq[Boolean] = latePaymentPenalties.map(_.isPaid)

  val secondLPP: Option[LatePaymentPenalty] = latePaymentPenalties.find(_.penaltyType.equals(LPPPenaltyCategoryEnum.LPP2))

  val firstLPPisPaid: Boolean = latePaymentPenalties.filter(_.penaltyType.equals(LPPPenaltyCategoryEnum.LPP1)).map(_.isPaid).forall(_ == true)

}
