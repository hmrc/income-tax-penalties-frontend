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

package connectors

import config.AppConfig
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import utils.ExceptionUtils.FutureBodyFunctionImplicits

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

object PenaltiesConnector {

  private def jsonObjNoNulls(fields: (String, Json.JsValueWrapper)*): JsObject = JsObject(Json.obj(fields:_*).fields.filterNot(_._2 == JsNull).filterNot(_._2 == Json.obj()))

  case class Totalisations(
    LSPTotalValue: Option[BigDecimal],
    penalisedPrincipalTotal: Option[BigDecimal],
    LPPPostedTotal: Option[BigDecimal],
    LPPEstimatedTotal: Option[BigDecimal],
    //NOTE: Below does not come from 1812, it is added data from 1811
    totalAccountOverdue: Option[BigDecimal],
    totalAccountPostedInterest: Option[BigDecimal],
    totalAccountAccruingInterest: Option[BigDecimal]
  )
  implicit val totalisationsFmt: Format[Totalisations] = Json.format[Totalisations]

  case class LSPSummary(
    activePenaltyPoints: Int,
    inactivePenaltyPoints: Int,
    regimeThreshold: Int,
    penaltyChargeAmount: BigDecimal,
    PoCAchievementDate: Option[LocalDate]
  )
  implicit val lspSummaryFmt: Format[LSPSummary] = Json.format[LSPSummary]

  object LSPPenaltyCategoryEnum extends Enumeration {
    val Point: LSPPenaltyCategoryEnum.Value = Value("P")
    val Threshold: LSPPenaltyCategoryEnum.Value = Value("T")
    val Charge: LSPPenaltyCategoryEnum.Value = Value("C")

    implicit val format: Format[LSPPenaltyCategoryEnum.Value] = new Format[LSPPenaltyCategoryEnum.Value] {
      override def writes(o: LSPPenaltyCategoryEnum.Value): JsValue = JsString(o.toString.toUpperCase)

      override def reads(json: JsValue): JsResult[LSPPenaltyCategoryEnum.Value] = json.as[String].toUpperCase match {
        case "P" => JsSuccess(Point)
        case "T" => JsSuccess(Threshold)
        case "C" => JsSuccess(Charge)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  object LSPPenaltyStatusEnum extends Enumeration {
    val Active: LSPPenaltyStatusEnum.Value = Value("ACTIVE")
    val Inactive: LSPPenaltyStatusEnum.Value = Value("INACTIVE")

    implicit val format: Format[LSPPenaltyStatusEnum.Value] = new Format[LSPPenaltyStatusEnum.Value] {
      override def writes(o: LSPPenaltyStatusEnum.Value): JsValue = JsString(o.toString)

      override def reads(json: JsValue): JsResult[LSPPenaltyStatusEnum.Value] = json.as[String].toUpperCase match {
        case "ACTIVE" => JsSuccess(Active)
        case "INACTIVE" => JsSuccess(Inactive)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  object ExpiryReasonEnum extends Enumeration {
    val Appeal: ExpiryReasonEnum.Value = Value("APP")
    val Adjustment: ExpiryReasonEnum.Value = Value("FAP")
    val Reversal: ExpiryReasonEnum.Value = Value("ICR")
    val Manual: ExpiryReasonEnum.Value = Value("MAN")
    val NaturalExpiration: ExpiryReasonEnum.Value = Value("NAT")
    val SubmissionOnTime: ExpiryReasonEnum.Value = Value("NLT")
    val Compliance: ExpiryReasonEnum.Value = Value("POC")
    val Reset: ExpiryReasonEnum.Value = Value("RES")

    implicit val format: Format[ExpiryReasonEnum.Value] = new Format[ExpiryReasonEnum.Value] {

      override def writes(o: ExpiryReasonEnum.Value): JsValue = JsString(o.toString.toUpperCase)

      override def reads(json: JsValue): JsResult[ExpiryReasonEnum.Value] = json.as[String].toUpperCase match {
        case "APP" => JsSuccess(Appeal)
        case "FAP" => JsSuccess(Adjustment)
        case "ICR" => JsSuccess(Reversal)
        case "MAN" => JsSuccess(Manual)
        case "NAT" => JsSuccess(NaturalExpiration)
        case "NLT" => JsSuccess(SubmissionOnTime)
        case "POC" => JsSuccess(Compliance)
        case "RES" => JsSuccess(Reset)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  object TaxReturnStatusEnum extends Enumeration {
    val Open: TaxReturnStatusEnum.Value = Value
    val Fulfilled: TaxReturnStatusEnum.Value = Value
    val Reversed: TaxReturnStatusEnum.Value = Value

    implicit val format: Format[TaxReturnStatusEnum.Value] = new Format[TaxReturnStatusEnum.Value] {
      override def writes(o: TaxReturnStatusEnum.Value): JsValue = JsString(o.toString)

      override def reads(json: JsValue): JsResult[TaxReturnStatusEnum.Value] = json.as[String].toUpperCase match {
        case "OPEN" => JsSuccess(Open)
        case "FULFILLED" => JsSuccess(Fulfilled)
        case "REVERSED" => JsSuccess(Reversed)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  case class LateSubmission(
    taxPeriodStartDate: Option[LocalDate],
    taxPeriodEndDate: Option[LocalDate],
    taxPeriodDueDate: Option[LocalDate],
    returnReceiptDate: Option[LocalDate],
    taxReturnStatus: Option[TaxReturnStatusEnum.Value]
  )
  implicit val lateSubmissionFmt: Format[LateSubmission] = Json.format[LateSubmission]

  object AppealStatusEnum extends Enumeration {
    val Under_Appeal: AppealStatusEnum.Value = Value("A")
    val Upheld: AppealStatusEnum.Value = Value("B")
    val Rejected: AppealStatusEnum.Value = Value("C")
    val Unappealable: AppealStatusEnum.Value = Value("99")

    implicit val format: Format[AppealStatusEnum.Value] = new Format[AppealStatusEnum.Value] {
      override def writes(o: AppealStatusEnum.Value): JsValue = JsString(o.toString)

      override def reads(json: JsValue): JsResult[AppealStatusEnum.Value] = json.as[String].toUpperCase match {
        case "A" => JsSuccess(Under_Appeal)
        case "B" => JsSuccess(Upheld)
        case "C" => JsSuccess(Rejected)
        case "99" => JsSuccess(Unappealable)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  object AppealLevelEnum extends Enumeration {
    val HMRC: AppealLevelEnum.Value = Value("01")
    val Tribunal: AppealLevelEnum.Value = Value("02")

    implicit val format: Format[AppealLevelEnum.Value] = new Format[AppealLevelEnum.Value] {
      override def writes(o: AppealLevelEnum.Value): JsValue = JsString(o.toString)

      override def reads(json: JsValue): JsResult[AppealLevelEnum.Value] = json.as[String].toUpperCase match {
        case "01" => JsSuccess(HMRC)
        case "02" => JsSuccess(Tribunal)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  case class AppealInformationType(
    appealStatus: Option[AppealStatusEnum.Value],
    appealLevel: Option[AppealLevelEnum.Value]
  )
  implicit val appealInformationTypeFmt: OFormat[AppealInformationType] = Json.format[AppealInformationType]

  object LSPTypeEnum extends Enumeration {
    val AddedFAP: LSPTypeEnum.Value = Value("AF")
    val RemovedFAP: LSPTypeEnum.Value = Value("RF")
    val AppealedPoint: LSPTypeEnum.Value = Value("AP")
    val RemovedPoint: LSPTypeEnum.Value = Value("RP")
    val Point: LSPTypeEnum.Value = Value("P")
    val Financial: LSPTypeEnum.Value = Value("F")

    implicit val format: Format[LSPTypeEnum.Value] = new Format[LSPTypeEnum.Value] {
      override def writes(o: LSPTypeEnum.Value): JsValue = JsString(o.toString.toUpperCase)

      override def reads(json: JsValue): JsResult[LSPTypeEnum.Value] = json.as[String].toUpperCase match {
        case "AF" => JsSuccess(AddedFAP)
        case "RF" => JsSuccess(RemovedFAP)
        case "AP" => JsSuccess(AppealedPoint)
        case "RP" => JsSuccess(RemovedPoint)
        case "P" => JsSuccess(Point)
        case "F" => JsSuccess(Financial)
      }
    }
  }

  case class LSPDetails(
    penaltyNumber: String,
    penaltyOrder: Option[String],
    penaltyCategory: Option[LSPPenaltyCategoryEnum.Value],
    penaltyStatus: LSPPenaltyStatusEnum.Value,
    incomeSourceName: Option[String],
    FAPIndicator: Option[String],
    penaltyCreationDate: LocalDate,
    penaltyExpiryDate: LocalDate,
    expiryReason: Option[ExpiryReasonEnum.Value],
    communicationsDate: Option[LocalDate],
    lateSubmissions: Option[Seq[LateSubmission]],
    appealInformation: Option[Seq[AppealInformationType]],
    chargeAmount: Option[BigDecimal],
    chargeOutstandingAmount: Option[BigDecimal],
    chargeDueDate: Option[LocalDate],
    lspTypeEnum: Option[LSPTypeEnum.Value] = None
  )
  implicit val lspDetailsFmt: Format[LSPDetails] = Json.format[LSPDetails]

  case class LateSubmissionPenalty(
    summary: LSPSummary,
    details: Seq[LSPDetails]
  )
  implicit val lateSubmissionPenaltyFmt: Format[LateSubmissionPenalty] = Json.format[LateSubmissionPenalty]

  object LPPPenaltyCategoryEnum extends Enumeration {
    val LPP1: LPPPenaltyCategoryEnum.Value = Value("LPP1")
    val LPP2: LPPPenaltyCategoryEnum.Value = Value("LPP2")
    val MANUAL: LPPPenaltyCategoryEnum.Value = Value("MANUAL")

    private val categories: Seq[LPPPenaltyCategoryEnum.Value] = Seq(LPP1, LPP2)

    def find(name: String): Option[LPPPenaltyCategoryEnum.Value] = categories.find(_.toString == name)

    implicit val format: Format[LPPPenaltyCategoryEnum.Value] = new Format[LPPPenaltyCategoryEnum.Value] {
      override def writes(o: LPPPenaltyCategoryEnum.Value): JsValue = JsString(o.toString.toUpperCase)

      override def reads(json: JsValue): JsResult[LPPPenaltyCategoryEnum.Value] = json.as[String].toUpperCase match {
        case "LPP1" => JsSuccess(LPP1)
        case "LPP2" => JsSuccess(LPP2)
        case "MANUAL" => JsSuccess(MANUAL)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  object LPPPenaltyStatusEnum extends Enumeration {
    val Accruing: LPPPenaltyStatusEnum.Value = Value("A")
    val Posted: LPPPenaltyStatusEnum.Value = Value("P")

    implicit val format: Format[LPPPenaltyStatusEnum.Value] = new Format[LPPPenaltyStatusEnum.Value] {
      override def writes(o: LPPPenaltyStatusEnum.Value): JsValue = JsString(o.toString.toUpperCase)

      override def reads(json: JsValue): JsResult[LPPPenaltyStatusEnum.Value] = json.as[String].toUpperCase match {
        case "A" => JsSuccess(Accruing)
        case "P" => JsSuccess(Posted)
        case e => JsError(s"$e not recognised")
      }
    }
  }

  case class LPPDetails(
                         principalChargeReference: String,
                         penaltyCategory: LPPPenaltyCategoryEnum.Value,
                         penaltyChargeCreationDate: Option[LocalDate],
                         penaltyStatus: LPPPenaltyStatusEnum.Value,
                         penaltyAmountPaid: Option[BigDecimal],
                         penaltyAmountPosted: BigDecimal,
                         penaltyAmountAccruing: BigDecimal,
                         penaltyAmountOutstanding: Option[BigDecimal],
                         LPP1LRDays: Option[String],
                         LPP1HRDays: Option[String],
                         LPP2Days: Option[String],
                         LPP1LRCalculationAmount: Option[BigDecimal],
                         LPP1HRCalculationAmount: Option[BigDecimal],
                         LPP1LRPercentage: Option[BigDecimal],
                         LPP1HRPercentage: Option[BigDecimal],
                         LPP2Percentage: Option[BigDecimal],
                         communicationsDate: Option[LocalDate],
                         penaltyChargeDueDate: Option[LocalDate],
                         appealInformation: Option[Seq[AppealInformationType]],
                         principalChargeBillingFrom: LocalDate,
                         principalChargeBillingTo: LocalDate,
                         principalChargeDueDate: LocalDate,
                         penaltyChargeReference: Option[String],
                         principalChargeLatestClearing: Option[LocalDate],
                         principalChargeDocNumber: String,
                         principalChargeMainTransaction: String,
                         principalChargeSubTransaction: String,
                         timeToPay: Option[Seq[TimeToPay]]
                       ) extends Ordered[LPPDetails] {
    override def compare(that: LPPDetails): Int = {
      (this.principalChargeBillingFrom, that.principalChargeBillingFrom,
        this.principalChargeBillingTo, that.principalChargeBillingTo,
        this.penaltyCategory, that.penaltyCategory)
      match {
        //Compare tax period start dates
        case (startDateA, startDateB, _, _, _, _) if startDateA.isBefore(startDateB) => 1
        case (startDateA, startDateB, _, _, _, _) if startDateA.isAfter(startDateB) => -1

        //Compare tax period end dates
        case (_, _, endDateA, endDateB, _, _) if endDateA.isBefore(endDateB) => 1
        case (_, _, endDateA, endDateB, _, _) if endDateA.isAfter(endDateB) => -1

        //Compare penaltyCategory
        case (_, _, _, _, categoryA, categoryB) if categoryA < categoryB => 1
        case (_, _, _, _, categoryA, categoryB) if categoryA > categoryB => -1

        //No difference found between this and that (will use ETMP order)
        case _ => 0
      }
    }
  }

    implicit val format: Format[LPPDetails] = new Format[LPPDetails] {
      override def reads(json: JsValue): JsResult[LPPDetails] = {
        for {
          principalChargeReference <- (json \ "principalChargeReference").validate[String]
          penaltyCategory <- (json \ "penaltyCategory").validate[LPPPenaltyCategoryEnum.Value]
          penaltyChargeCreationDate <- (json \ "penaltyChargeCreationDate").validateOpt[LocalDate]
          penaltyStatus <- (json \ "penaltyStatus").validate[LPPPenaltyStatusEnum.Value]
          penaltyAmountPaid <- (json \ "penaltyAmountPaid").validateOpt[BigDecimal]
          penaltyAmountPosted <- (json \ "penaltyAmountPosted").validate[BigDecimal]
          penaltyAmountAccruing <- (json \ "penaltyAmountAccruing").validate[BigDecimal]
          penaltyAmountOutstanding <- (json \ "penaltyAmountOutstanding").validateOpt[BigDecimal]
          lPP1LRDays <- (json \ "LPP1LRDays").validateOpt[String]
          lPP1HRDays <- (json \ "LPP1HRDays").validateOpt[String]
          lPP2Days <- (json \ "LPP2Days").validateOpt[String]
          lPP1LRCalculationAmount <- (json \ "LPP1LRCalculationAmount").validateOpt[BigDecimal]
          lPP1HRCalculationAmount <- (json \ "LPP1HRCalculationAmount").validateOpt[BigDecimal]
          lPP1LRPercentage <- (json \ "LPP1LRPercentage").validateOpt[BigDecimal]
          lPP1HRPercentage <- (json \ "LPP1HRPercentage").validateOpt[BigDecimal]
          lPP2Percentage <- (json \ "LPP2Percentage").validateOpt[BigDecimal]
          communicationsDate <- (json \ "communicationsDate").validateOpt[LocalDate]
          penaltyChargeDueDate <- (json \ "penaltyChargeDueDate").validateOpt[LocalDate]
          appealInformation <- (json \ "appealInformation").validateOpt[Seq[AppealInformationType]]
          principalChargeBillingFrom <- (json \ "principalChargeBillingFrom").validate[LocalDate]
          principalChargeBillingTo <- (json \ "principalChargeBillingTo").validate[LocalDate]
          principalChargeDueDate <- (json \ "principalChargeDueDate").validate[LocalDate]
          penaltyChargeReference <- (json \ "penaltyChargeReference").validateOpt[String]
          principalChargeLatestClearing <- (json \ "principalChargeLatestClearing").validateOpt[LocalDate]
          principalChargeDocNumber <- (json \ "principalChargeDocNumber").validate[String]
          principalChargeMainTransaction <- (json \ "principalChargeMainTransaction").validate[String]
          principalChargeSubTransaction <- (json \ "principalChargeSubTransaction").validate[String]
          timeToPay <- (json \ "timeToPay").validateOpt[Seq[TimeToPay]]
        }
        yield {
          LPPDetails(principalChargeReference, penaltyCategory, penaltyChargeCreationDate, penaltyStatus, penaltyAmountPaid,
            penaltyAmountPosted, penaltyAmountAccruing, penaltyAmountOutstanding, lPP1LRDays, lPP1HRDays, lPP2Days, lPP1LRCalculationAmount,
            lPP1HRCalculationAmount, lPP1LRPercentage, lPP1HRPercentage, lPP2Percentage, communicationsDate, penaltyChargeDueDate, appealInformation,
            principalChargeBillingFrom, principalChargeBillingTo, principalChargeDueDate, penaltyChargeReference,
            principalChargeLatestClearing, principalChargeDocNumber, principalChargeMainTransaction, principalChargeSubTransaction, timeToPay)
        }
      }
      override def writes(o: LPPDetails): JsValue = {
        jsonObjNoNulls(
          "principalChargeReference" -> o.principalChargeReference,
          "penaltyCategory" -> o.penaltyCategory,
          "penaltyChargeCreationDate" -> o.penaltyChargeCreationDate,
          "penaltyStatus" -> o.penaltyStatus,
          "penaltyAmountPaid" -> o.penaltyAmountPaid,
          "penaltyAmountPosted" -> o.penaltyAmountPosted,
          "penaltyAmountAccruing" -> o.penaltyAmountAccruing,
          "penaltyAmountOutstanding" -> o.penaltyAmountOutstanding,
          "LPP1LRDays" -> o.LPP1LRDays,
          "LPP1HRDays" -> o.LPP1HRDays,
          "LPP2Days" -> o.LPP2Days,
          "LPP1LRCalculationAmount" -> o.LPP1LRCalculationAmount,
          "LPP1HRCalculationAmount" -> o.LPP1HRCalculationAmount,
          "LPP1LRPercentage" -> o.LPP1LRPercentage,
          "LPP1HRPercentage" -> o.LPP1HRPercentage,
          "LPP2Percentage" -> o.LPP2Percentage,
          "communicationsDate" -> o.communicationsDate,
          "penaltyChargeDueDate" -> o.penaltyChargeDueDate,
          "appealInformation" -> o.appealInformation,
          "principalChargeBillingFrom" -> o.principalChargeBillingFrom,
          "principalChargeBillingTo" -> o.principalChargeBillingTo,
          "principalChargeDueDate" -> o.principalChargeDueDate,
          "penaltyChargeReference" -> o.penaltyChargeReference,
          "principalChargeLatestClearing" -> o.principalChargeLatestClearing,
          "principalChargeDocNumber" -> o.principalChargeDocNumber,
          "principalChargeMainTransaction" -> o.principalChargeMainTransaction,
          "principalChargeSubTransaction" -> o.principalChargeSubTransaction,
          "timeToPay" -> o.timeToPay
        )
      }
    }

  object MainTransactionEnum extends Enumeration {
    val VATReturnCharge: MainTransactionEnum.Value = Value("4700")
    val VATReturnFirstLPP: MainTransactionEnum.Value = Value("4703")
    val VATReturnSecondLPP: MainTransactionEnum.Value = Value("4704")
    val CentralAssessment: MainTransactionEnum.Value = Value("4720")
    val CentralAssessmentFirstLPP: MainTransactionEnum.Value = Value("4723")
    val CentralAssessmentSecondLPP: MainTransactionEnum.Value = Value("4724")
    val OfficersAssessment: MainTransactionEnum.Value = Value("4730")
    val OfficersAssessmentFirstLPP: MainTransactionEnum.Value = Value("4741")
    val OfficersAssessmentSecondLPP: MainTransactionEnum.Value = Value("4742")
    val ErrorCorrection: MainTransactionEnum.Value = Value("4731")
    val ErrorCorrectionFirstLPP: MainTransactionEnum.Value = Value("4743")
    val ErrorCorrectionSecondLPP: MainTransactionEnum.Value = Value("4744")
    val AdditionalAssessment: MainTransactionEnum.Value = Value("4732")
    val AdditionalAssessmentFirstLPP: MainTransactionEnum.Value = Value("4758")
    val AdditionalAssessmentSecondLPP: MainTransactionEnum.Value = Value("4759")
    val ProtectiveAssessment: MainTransactionEnum.Value = Value("4733")
    val ProtectiveAssessmentFirstLPP: MainTransactionEnum.Value = Value("4761")
    val ProtectiveAssessmentSecondLPP: MainTransactionEnum.Value = Value("4762")
    val VATOverpaymentForTax: MainTransactionEnum.Value = Value("4764")
    val POAReturnCharge: MainTransactionEnum.Value = Value("4701")
    val POAReturnChargeFirstLPP: MainTransactionEnum.Value = Value("4716")
    val POAReturnChargeSecondLPP: MainTransactionEnum.Value = Value("4717")
    val AAReturnCharge: MainTransactionEnum.Value = Value("4702")
    val AAReturnChargeFirstLPP: MainTransactionEnum.Value = Value("4718")
    val AAReturnChargeSecondLPP: MainTransactionEnum.Value = Value("4719")
    val ManualCharge: MainTransactionEnum.Value = Value("4787")

    implicit val format: Format[MainTransactionEnum.Value] = new Format[MainTransactionEnum.Value] {
      override def writes(o: MainTransactionEnum.Value): JsValue = {
        JsString(o.toString)
      }

      override def reads(json: JsValue): JsResult[MainTransactionEnum.Value] = {
        json.as[String] match {
          case "4700" => JsSuccess(VATReturnCharge)
          case "4703" => JsSuccess(VATReturnFirstLPP)
          case "4704" => JsSuccess(VATReturnSecondLPP)
          case "4720" => JsSuccess(CentralAssessment)
          case "4723" => JsSuccess(CentralAssessmentFirstLPP)
          case "4724" => JsSuccess(CentralAssessmentSecondLPP)
          case "4730" => JsSuccess(OfficersAssessment)
          case "4741" => JsSuccess(OfficersAssessmentFirstLPP)
          case "4742" => JsSuccess(OfficersAssessmentSecondLPP)
          case "4731" => JsSuccess(ErrorCorrection)
          case "4743" => JsSuccess(ErrorCorrectionFirstLPP)
          case "4744" => JsSuccess(ErrorCorrectionSecondLPP)
          case "4732" => JsSuccess(AdditionalAssessment)
          case "4758" => JsSuccess(AdditionalAssessmentFirstLPP)
          case "4759" => JsSuccess(AdditionalAssessmentSecondLPP)
          case "4733" => JsSuccess(ProtectiveAssessment)
          case "4761" => JsSuccess(ProtectiveAssessmentFirstLPP)
          case "4762" => JsSuccess(ProtectiveAssessmentSecondLPP)
          case "4764" => JsSuccess(VATOverpaymentForTax)
          case "4701" => JsSuccess(POAReturnCharge)
          case "4716" => JsSuccess(POAReturnChargeFirstLPP)
          case "4717" => JsSuccess(POAReturnChargeSecondLPP)
          case "4702" => JsSuccess(AAReturnCharge)
          case "4718" => JsSuccess(AAReturnChargeFirstLPP)
          case "4719" => JsSuccess(AAReturnChargeSecondLPP)
          case "4787" => JsSuccess(ManualCharge)
          case e => JsError(s"$e not recognised")
        }
      }
    }
  }

  case class TimeToPay(
    TTPStartDate: Option[LocalDate],
    TTPEndDate: Option[LocalDate]
  )
  implicit val timeToPayFmt: OFormat[TimeToPay] = Json.format[TimeToPay]

  case class LatePaymentPenalty(details: Seq[LPPDetails], manualLPPIndicator: Boolean)
  implicit val latePaymentPenaltyFmt: Format[LatePaymentPenalty] = Json.format[LatePaymentPenalty]

  case class BreathingSpace(BSStartDate: LocalDate, BSEndDate: LocalDate)
  implicit val breathingSpaceFmt: Format[BreathingSpace] = Json.format[BreathingSpace]

  case class GetPenaltyDetails(
    totalisations: Option[Totalisations],
    lateSubmissionPenalty: Option[LateSubmissionPenalty],
    latePaymentPenalty: Option[LatePaymentPenalty],
    breathingSpace: Option[Seq[BreathingSpace]]
  )
  implicit val getPenaltyDetailsFmt: Format[GetPenaltyDetails] = Json.format[GetPenaltyDetails]
}

class PenaltiesConnector @Inject()(httpClient: HttpClientV2,
                                   val appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {
  import PenaltiesConnector._

  private def penaltiesServiceUrl = appConfig.penaltiesService.resolve

  def getPenaltyDetails(enrolmentKey: String)(implicit hc: HeaderCarrier): Future[GetPenaltyDetails] = {
    logger.info(s"[PenaltiesConnector][getPenaltyDetails] - Requesting penalties details from backend for VRN $enrolmentKey.")

    httpClient.get(penaltiesServiceUrl(s"etmp/penalties/$enrolmentKey")).execute.delayFailure.transform(
      { case response if response.status == 200 =>
          logger.debug(Json.prettyPrint(response.json))
          response.json.as[GetPenaltyDetails]
        case response =>
          throw new Exception(s"Backend responded with ${response.status}")
      }, {
        (th: Throwable) => th
      }
    )
  }
}
