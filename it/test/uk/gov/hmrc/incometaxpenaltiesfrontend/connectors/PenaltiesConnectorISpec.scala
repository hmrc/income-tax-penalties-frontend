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

package uk.gov.hmrc.incometaxpenaltiesfrontend.connectors

import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.ComplianceDataParser.{ComplianceDataMalformed, ComplianceDataNoData, ComplianceDataResponse, ComplianceDataUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.{GetPenaltyDetailsBadRequest, GetPenaltyDetailsMalformed, GetPenaltyDetailsResponse, GetPenaltyDetailsUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.{ComplianceData, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp._
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{PenaltyDetails, Totalisations}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import java.time.LocalDate

class PenaltiesConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods {

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]

  val sampleComplianceData: ComplianceData = ComplianceData(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
    )),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 2, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      )
    )
  )

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
    FAPIndicator = None,
    penaltyCreationDate = creationDate,
    penaltyExpiryDate = expiryDate,
    expiryReason = None,
    communicationsDate = Some(creationDate),
    lateSubmissions = Some(Seq(
      LateSubmission(
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
    lspTypeEnum = Some(LSPTypeEnum.Point)
  )


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
    lateSubmissionPenalty = Some(LateSubmissionPenalty(
      summary = LSPSummary(
        activePenaltyPoints = 1, inactivePenaltyPoints = 0, regimeThreshold = 4, penaltyChargeAmount = 200, PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
      ),
      details = Seq(sampleLateSubmissionPoint))),
    latePaymentPenalty = Some(
      LatePaymentPenalty(
        Seq(
          sampleUnpaidLPP1.copy(LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(MainTransactionEnum.VATReturnFirstLPP), outstandingAmount = Some(20), timeToPay = None))
        )
      )
    ),
    breathingSpace = None
  )

  val samplePenaltyDetailsModelWithoutMetadata: PenaltyDetails = samplePenaltyDetailsModel.copy(latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleUnpaidLPP1))))


  val testMtditid: String = "1234567890"
  val testArn: String = "XARN1234567890"
  val testFromDate: LocalDate = LocalDate.of(2020, 1, 1)
  val testToDate: LocalDate = LocalDate.of(2020, 12, 1)

  "getPenaltyDetails" should {
    "return a successful response when the call succeeds and the body can be parsed" when {
      "an individual has Late Payment Metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid").thenReturn(status = OK, body = samplePenaltyDetailsModel)

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(testMtditid)(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "a client has Late Payment Metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid\\?arn=$testArn").thenReturn(status = OK, body = samplePenaltyDetailsModel)

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(testMtditid, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "an individual does not have metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid").thenReturn(status = OK, body = samplePenaltyDetailsModelWithoutMetadata)

        val result = await(connector.getPenaltyDetails(testMtditid)(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }

      "a client does not have metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid\\?arn=$testArn").thenReturn(status = OK, body = samplePenaltyDetailsModelWithoutMetadata)

        val result = await(connector.getPenaltyDetails(testMtditid, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }

      "an individual does not have any penalty details" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid").thenReturn(status = NO_CONTENT, body = Json.obj())

        val result = await(connector.getPenaltyDetails(testMtditid)(HeaderCarrier()))

        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }

      "a client does not have any penalty details" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid\\?arn=$testArn").thenReturn(status = NO_CONTENT, body = Json.obj())

        val result = await(connector.getPenaltyDetails(testMtditid, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }
    }

    "return a Left when" when {
      "an exception with status 4xx occurs upstream" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid").thenReturn(status = BAD_REQUEST, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails(testMtditid)(HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result shouldBe Left(GetPenaltyDetailsBadRequest)
          }
        }
      }

      "invalid Json is returned" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid").thenReturn(status = OK, body = "")

        val result = await(connector.getPenaltyDetails(testMtditid)(HeaderCarrier()))

        result shouldBe Left(GetPenaltyDetailsMalformed)
      }

      "an exception with status 5xx occurs upstream is returned" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtditid").thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails(testMtditid)(HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result shouldBe Left(GetPenaltyDetailsUnexpectedFailure(INTERNAL_SERVER_ERROR))
          }
        }
      }
    }
  }

  "getObligationData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in {
      when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtditid&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
        .thenReturn(status = OK, body = sampleComplianceData)

      val result: ComplianceDataResponse = await(connector.getObligationData(testMtditid, testFromDate, testToDate)(HeaderCarrier()))

      result shouldBe Right(sampleComplianceData)
    }

    "return a Left response" when {
      "the call returns a OK response however the body is not parsable as a model" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtditid&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = OK, body = Json.obj("invalid" -> "json"))

        val result: ComplianceDataResponse = await(connector.getObligationData(testMtditid, testFromDate, testToDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataMalformed)
      }

      "the call returns a Not Found status" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtditid&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = NOT_FOUND, body = Json.obj())

        val result: ComplianceDataResponse = await(connector.getObligationData(testMtditid, testFromDate, testToDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataNoData)
      }

      "the call returns an unmatched response" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtditid&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = SERVICE_UNAVAILABLE, body = Json.obj())

        val result: ComplianceDataResponse = await(connector.getObligationData(testMtditid, testFromDate, testToDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataUnexpectedFailure(SERVICE_UNAVAILABLE))
      }

      "the call returns a UpstreamErrorResponse(4xx) exception" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtditid&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = BAD_REQUEST, body = Json.obj())


        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: ComplianceDataResponse = await(connector.getObligationData(testMtditid, testFromDate, testToDate)(HeaderCarrier()))

            result shouldBe Left(ComplianceDataUnexpectedFailure(BAD_REQUEST))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }

      "the call returns a UpstreamErrorResponse(5xx) exception" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtditid&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: ComplianceDataResponse = await(connector.getObligationData(testMtditid, testFromDate, testToDate)(HeaderCarrier()))

            result shouldBe Left(ComplianceDataUnexpectedFailure(INTERNAL_SERVER_ERROR))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }
    }
  }

}
