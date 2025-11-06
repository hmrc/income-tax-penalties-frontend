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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp

import play.api.libs.json._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyStatusEnum.Posted
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.JsonUtils

import java.time.LocalDate

case class  LPPDetails(principalChargeReference: String,
                      penaltyCategory: LPPPenaltyCategoryEnum.Value,
                      penaltyStatus: LPPPenaltyStatusEnum.Value,
                      penaltyAmountAccruing: BigDecimal,
                      penaltyAmountPosted: BigDecimal,
                      penaltyAmountPaid: Option[BigDecimal],
                      penaltyAmountOutstanding: Option[BigDecimal],
                      lpp1LRCalculationAmt: Option[BigDecimal],
                      lpp1LRDays: Option[String],
                      lpp1LRPercentage: Option[BigDecimal],
                      lpp1HRCalculationAmt: Option[BigDecimal],
                      lpp1HRDays: Option[String],
                      lpp1HRPercentage: Option[BigDecimal],
                      lpp2Days: Option[String],
                      lpp2Percentage: Option[BigDecimal],
                      penaltyChargeCreationDate: Option[LocalDate],
                      communicationsDate: Option[LocalDate],
                      penaltyChargeReference: Option[String],
                      penaltyChargeDueDate: Option[LocalDate],
                      appealInformation: Option[Seq[AppealInformationType]],
                      principalChargeBillingFrom: LocalDate,
                      principalChargeBillingTo: LocalDate,
                      principalChargeDueDate: LocalDate,
                      principalChargeLatestClearing: Option[LocalDate],
                      vatOutstandingAmount: Option[BigDecimal],
                      metadata: LPPDetailsMetadata) extends Ordered[LPPDetails] {

  private val AppealInfoWithHighestAppealLevel: Option[AppealInformationType] = appealInformation.flatMap(_.maxByOption(_.appealLevel.map(_.id).getOrElse(-1)))
  val appealStatus: Option[AppealStatusEnum.Value] = AppealInfoWithHighestAppealLevel.flatMap(_.appealStatus)
  val appealLevel: Option[AppealLevelEnum.Value] = AppealInfoWithHighestAppealLevel.flatMap(_.appealLevel)

  val amountDue: BigDecimal = if (penaltyStatus == Posted) penaltyAmountPosted else penaltyAmountAccruing

  //TODO: Expect an API change to return a different name other than `vat` prefixed for the outstandingAmount
  val incomeTaxOutstandingAmountInPence: Int = vatOutstandingAmount.map(amount => (amount * 100).toInt).getOrElse(0)

  val isPaid: Boolean = !penaltyAmountPaid.contains(0) && penaltyAmountPaid.contains(penaltyAmountPosted)
  val incomeTaxIsPaid: Boolean = principalChargeLatestClearing.isDefined

  override def compare(that: LPPDetails): Int = {
    (this.principalChargeBillingFrom, that.principalChargeBillingFrom,
      this.principalChargeBillingTo, that.principalChargeBillingTo,
      this.metadata.principalChargeMainTr, that.metadata.principalChargeMainTr,
      this.penaltyCategory, that.penaltyCategory)
    match {
      //Compare tax period start dates
      case (startDateA, startDateB, _, _, _, _, _, _) if startDateA.isBefore(startDateB) => 1
      case (startDateA, startDateB, _, _, _, _, _, _) if startDateA.isAfter(startDateB) => -1

      //Compare tax period end dates
      case (_, _, endDateA, endDateB, _, _, _, _) if endDateA.isBefore(endDateB) => 1
      case (_, _, endDateA, endDateB, _, _, _, _) if endDateA.isAfter(endDateB) => -1

      //Compare mainTransactions
      case (_, _, _, _, mainTransA, mainTransB, _, _) if mainTransA < mainTransB => 1
      case (_, _, _, _, mainTransA, mainTransB, _, _) if mainTransA > mainTransB => -1

      //Compare penaltyCategory
      case (_, _, _, _, _, _, categoryA, categoryB) if categoryA < categoryB => 1
      case (_, _, _, _, _, _, categoryA, categoryB) if categoryA > categoryB => -1

      //No difference found between this and that (will use ETMP order)
      case _ => 0
    }
  }

}

object LPPDetails extends JsonUtils {
  implicit val format: Format[LPPDetails] = new Format[LPPDetails] {
    override def reads(json: JsValue): JsResult[LPPDetails] = {
      for {
        principalChargeReference <- (json \ "principalChargeReference").validate[String]
        penaltyCategory <- (json \ "penaltyCategory").validate[LPPPenaltyCategoryEnum.Value]
        penaltyStatus <- (json \ "penaltyStatus").validate[LPPPenaltyStatusEnum.Value]
        penaltyAmountAccruing <- (json \ "penaltyAmountAccruing").validate[BigDecimal]
        penaltyAmountPosted <- (json \ "penaltyAmountPosted").validate[BigDecimal]
        penaltyAmountPaid <- (json \ "penaltyAmountPaid").validateOpt[BigDecimal]
        penaltyAmountOutstanding <- (json \ "penaltyAmountOutstanding").validateOpt[BigDecimal]
        lpp1LRCalculationAmt <- (json \ "lpp1LRCalculationAmt").validateOpt[BigDecimal]
        lpp1LRDays <- (json \ "lpp1LRDays").validateOpt[String]
        lpp1LRPercentage <- (json \ "lpp1LRPercentage").validateOpt[BigDecimal]
        lpp1HRCalculationAmt <- (json \ "lpp1HRCalculationAmt").validateOpt[BigDecimal]
        lpp1HRDays <- (json \ "lpp1HRDays").validateOpt[String]
        lpp1HRPercentage <- (json \ "lpp1HRPercentage").validateOpt[BigDecimal]
        lpp2Days <- (json \ "lpp2Days").validateOpt[String]
        lpp2Percentage <- (json \ "lpp2Percentage").validateOpt[BigDecimal]
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
        metadata <- Json.fromJson(json)(LPPDetailsMetadata.format)
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
    }

    override def writes(o: LPPDetails): JsValue = {
      jsonObjNoNulls(
        "penaltyCategory" -> o.penaltyCategory,
        "penaltyChargeReference" -> o.penaltyChargeReference,
        "principalChargeReference" -> o.principalChargeReference,
        "penaltyChargeCreationDate" -> o.penaltyChargeCreationDate,
        "penaltyStatus" -> o.penaltyStatus,
        "appealInformation" -> o.appealInformation,
        "principalChargeBillingFrom" -> o.principalChargeBillingFrom,
        "principalChargeBillingTo" -> o.principalChargeBillingTo,
        "principalChargeDueDate" -> o.principalChargeDueDate,
        "communicationsDate" -> o.communicationsDate,
        "penaltyAmountOutstanding" -> o.penaltyAmountOutstanding,
        "penaltyAmountPosted" -> o.penaltyAmountPosted,
        "penaltyAmountPaid" -> o.penaltyAmountPaid,
        "lpp1LRDays" -> o.lpp1LRDays,
        "lpp1HRDays" -> o.lpp1HRDays,
        "lpp2Days" -> o.lpp2Days,
        "lpp1HRCalculationAmt" -> o.lpp1HRCalculationAmt,
        "lpp1LRCalculationAmt" -> o.lpp1LRCalculationAmt,
        "timeToPay" -> o.metadata.timeToPay,
        "lpp2Percentage" -> o.lpp2Percentage,
        "lpp1LRPercentage" -> o.lpp1LRPercentage,
        "lpp1HRPercentage" -> o.lpp1HRPercentage,
        "penaltyChargeDueDate" -> o.penaltyChargeDueDate,
        "principalChargeLatestClearing" -> o.principalChargeLatestClearing,
        "penaltyAmountAccruing" -> o.penaltyAmountAccruing,
        "principalChargeMainTr" -> o.metadata.principalChargeMainTr,
        "vatOutstandingAmount" -> o.vatOutstandingAmount,
        "principalChargeDocNumber" -> o.metadata.principalChargeDocNumber,
        "principalChargeSubTr" -> o.metadata.principalChargeSubTr
      )
    }
  }
}

case class LPPDetailsMetadata(
                               principalChargeMainTr: String,
                               timeToPay: Option[Seq[TimeToPay]],
                               principalChargeDocNumber: Option[String] = None,
                               principalChargeSubTr: Option[String] = None
                             )

object LPPDetailsMetadata {
  implicit val format: OFormat[LPPDetailsMetadata] = Json.format[LPPDetailsMetadata]
}

case class TimeToPay(
                      ttpStartDate: Option[LocalDate],
                      ttpEndDate: Option[LocalDate]
                    )

object TimeToPay {
  implicit val format: OFormat[TimeToPay] = Json.format[TimeToPay]
}
