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

import connectors.PenaltiesConnector.{GetPenaltyDetails, LPPDetails, LSPDetails, TaxReturnStatusEnum}
import play.api.i18n.Messages
import utils.DisplayFormats.{LocalDateEx, displayDayMonthYear, displayMonthYear}

class Penalties(penaltyDetails: GetPenaltyDetails)(implicit messages: Messages) {

  private val lspSummary = penaltyDetails.lateSubmissionPenalty.map(_.summary)

  val totalLSPPs: Int = lspSummary.map(_.activePenaltyPoints).getOrElse(0)

  def regimeLSPThreshold: Int = lspSummary.map(_.regimeThreshold).get

  class LateSubmissionPenalty(lspDetails: LSPDetails) {
    private val headSubmission = lspDetails.lateSubmissions.head.last
    private val currSubmission = lspDetails.lateSubmissions.head.filter(_.taxReturnStatus.contains(TaxReturnStatusEnum.Fulfilled)).last
    val penaltyNumber: String = lspDetails.penaltyNumber
    val ordinal: String = lspDetails.penaltyOrder.map(_.toInt.toString).get // "1"
    val status: String = lspDetails.penaltyStatus.toString // "ACTIVE"
    val source: String = lspDetails.incomeSourceName.getOrElse("") // "JB Painting and Decorating"
    val quarterFrom: String = headSubmission.taxPeriodStartDate.toDayMonthYear // "6 April 2027"
    val quarterTo: String = headSubmission.taxPeriodEndDate.toDayMonthYear //"5 July 2027"
    val updateDue: String = headSubmission.taxPeriodDueDate.toDayMonthYear //"5 August 2027"
    val updateSubmitted: String = currSubmission.returnReceiptDate.map(displayDayMonthYear).getOrElse("Not yet received") // "10 August 2027" // "Not yet received"
    val dueToExpire: String = displayMonthYear(lspDetails.penaltyExpiryDate); // "September 2029"
  }

  val lateSubmissionPenalties: Seq[LateSubmissionPenalty] =
    penaltyDetails.lateSubmissionPenalty.map(_.details).getOrElse(Seq()).map(new LateSubmissionPenalty(_))

  class LatePaymentPenalty(lppDetails: LPPDetails) {
    /** placeholder for LPP functionality to be added */
  }

  val latePaymentPenalties: Seq[LatePaymentPenalty] =
    penaltyDetails.latePaymentPenalty.map(_.details).getOrElse(Seq()).map(new LatePaymentPenalty(_))

}
