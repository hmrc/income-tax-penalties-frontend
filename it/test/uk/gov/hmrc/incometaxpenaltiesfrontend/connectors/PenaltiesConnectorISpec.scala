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

import fixtures.{ComplianceDataTestData, PenaltiesFixture}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.ComplianceDataParser.{ComplianceDataMalformed, ComplianceDataNoData, ComplianceDataResponse, ComplianceDataUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.{GetPenaltyDetailsBadRequest, GetPenaltyDetailsMalformed, GetPenaltyDetailsResponse, GetPenaltyDetailsUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.{PenaltyDetails, PenaltySuccessResponse}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.ComplianceStub
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

class PenaltiesConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods with
  PenaltiesFixture with ComplianceDataTestData with ComplianceStub with FeatureSwitching {

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]
  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  "getPenaltyDetails" should {
    "return a successful response when the call succeeds and the HIP body can be parsed" when {
      "an individual has Late Payment Metadata" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = OK, body = PenaltySuccessResponse("22/01/2023", Some(samplePenaltyDetailsModel)))

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(testNino)(HeaderCarrier()))

        println(Json.prettyPrint(Json.toJson(samplePenaltyDetailsModel)))
        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "a client has Late Payment Metadata" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino\\?arn=$testArn").thenReturn(status = OK, body = PenaltySuccessResponse("22/01/2023", Some(samplePenaltyDetailsModel)))

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(testNino, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "an individual does not have metadata" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = OK, body = PenaltySuccessResponse("22/01/2023", Some(samplePenaltyDetailsModelWithoutMetadata)))

        val result = await(connector.getPenaltyDetails(testNino)(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }

      "a client does not have metadata" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino\\?arn=$testArn").thenReturn(status = OK, body = PenaltySuccessResponse("22/01/2023", Some(samplePenaltyDetailsModelWithoutMetadata)))

        val result = await(connector.getPenaltyDetails(testNino, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }

      "an individual does not have any penalty details" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = NO_CONTENT, body = Json.obj())

        val result = await(connector.getPenaltyDetails(testNino)(HeaderCarrier()))

        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }

      "a client does not have any penalty details" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino\\?arn=$testArn").thenReturn(status = NO_CONTENT, body = Json.obj())

        val result = await(connector.getPenaltyDetails(testNino, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }
    }

    "return a success response and convert to HIP model" when {
      "penalties returns a valid IF response" in {
        val ifPenaltiesResponse = Json.obj(
          "lateSubmissionPenalty" -> Json.obj(
            "summary" -> Json.obj(
              "activePenaltyPoints" -> 1,
              "inactivePenaltyPoints" -> 0,
              "PoCAchievementDate" -> "2027-11-07",
              "regimeThreshold" -> 4,
              "penaltyChargeAmount" -> 200.00
            ),
            "details" -> Json.arr(
              Json.obj(
                "penaltyCategory" -> "P",
                "penaltyNumber" -> "005000000328",
                "penaltyOrder" -> "1",
                "penaltyCreationDate" -> "2027-11-10",
                "penaltyExpiryDate" -> "2029-12-10",
                "penaltyStatus" -> "ACTIVE",
                "incomeSourceName" -> "JB Painting and Decorating",
                "triggeringProcess" -> "ICRU",
                "communicationsDate" -> "2027-11-10",
                "lateSubmissions" -> Json.arr(
                  Json.obj(
                    "lateSubmissionID" -> "001",
                    "taxPeriodStartDate" -> "2027-07-06",
                    "taxPeriodEndDate" -> "2027-10-05",
                    "taxPeriodDueDate" -> "2027-11-07",
                    "returnReceiptDate" -> "2027-11-10",
                    "taxReturnStatus" -> "Fulfilled"
                  )
                )
              )
            )
          ),
        "latePaymentPenalty" -> Json.obj(
          "details" -> Json.arr(
            Json.obj(
              "principalChargeReference" -> "XJ002616061027",
              "penaltyCategory" -> "LPP1",
              "penaltyStatus" -> "A",
              "penaltyAmountAccruing" -> 400.00,
              "penaltyAmountPosted" -> 0,
              "penaltyAmountPaid" -> 0,
              "penaltyAmountOutstanding" -> 0,
              "LPP1LRCalculationAmount" -> 20000.00,
              "LPP1LRDays" -> "15",
              "LPP1LRPercentage" -> 2,
              "LPP1HRDays" -> "30",
              "LPP1HRPercentage" -> 2,
              "penaltyChargeReference" -> "XJ002616061027",
              "principalChargeMainTransaction" -> "4720",
              "principalChargeBillingFrom" -> "2027-04-06",
              "principalChargeBillingTo" -> "2028-04-05",
              "principalChargeDueDate" -> "2029-01-31",
              "principalChargeDocNumber" -> "DOC1",
              "principalChargeSubTransaction" -> "SUB1"
            ),
            Json.obj(
              "principalChargeReference" -> "XJ002616061028",
              "penaltyCategory" -> "LPP1",
              "penaltyStatus" -> "P",
              "penaltyAmountAccruing" -> 0,
              "penaltyAmountPosted" -> 400.00,
              "penaltyAmountPaid" -> 400.00,
              "penaltyAmountOutstanding" -> 0,
              "LPP1LRCalculationAmount" -> 20000.00,
              "LPP1LRDays" -> "15",
              "LPP1LRPercentage" -> 2,
              "LPP1HRDays" -> "30",
              "LPP1HRPercentage" -> 2,
              "penaltyChargeReference" -> "XJ002616061028",
              "principalChargeMainTransaction" -> "4720",
              "principalChargeBillingFrom" -> "2026-04-06",
              "principalChargeBillingTo" -> "2027-04-05",
              "principalChargeDueDate" -> "2028-01-31",
              "principalChargeLatestClearing" -> "2028-02-19",
              "principalChargeDocNumber" -> "DOC1",
              "principalChargeSubTransaction" -> "SUB1"
            )
          )
        ))

        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = OK, body = ifPenaltiesResponse)

        val result = await(connector.getPenaltyDetails(testNino, None)(HeaderCarrier()))

        result.isRight shouldBe true
      }
    }

    "return a Left when" when {
      "an exception with status 4xx occurs upstream" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = BAD_REQUEST, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails(testNino)(HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result shouldBe Left(GetPenaltyDetailsBadRequest)
          }
        }
      }

      "invalid Json is returned" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = OK, body = Json.obj("success" -> "ERROR"))

        val result = await(connector.getPenaltyDetails(testNino)(HeaderCarrier()))

        result shouldBe Left(GetPenaltyDetailsMalformed)
      }

      "an exception with status 5xx occurs upstream is returned" in {
        when(GET, uri = s"/penalties/ITSA/etmp/penalties/NINO/$testNino").thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails(testNino)(HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result shouldBe Left(GetPenaltyDetailsUnexpectedFailure(INTERNAL_SERVER_ERROR))
          }
        }
      }
    }
  }

  "getComplianceData" should {
    "return a successful response" when {
      "the call succeeds and the body can be parsed" in {
        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayload))

        val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

        result shouldBe Right(sampleCompliancePayload)
      }
    }

    "return a successful response" when {
      "the stub succeeds and the body can be parsed" in {
        enable(UseStubForBackend)
        stubGetComplianceDataFromStub(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(sampleCompliancePayload))

        val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

        result shouldBe Right(sampleCompliancePayload)
      }
    }

    "return a Left response" when {
      "the call returns a OK response however the body is not parsable as a model" in {
        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(OK, Json.toJson(Json.obj("invalid" -> "json")))

        val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataMalformed)
      }

      "the call returns a Not Found status" in {
        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(NOT_FOUND, Json.toJson(Json.obj()))

        val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataNoData)
      }

      "the call returns an unmatched response" in {
        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(SERVICE_UNAVAILABLE, Json.toJson(Json.obj()))

        val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataUnexpectedFailure(SERVICE_UNAVAILABLE))
      }

      "the call returns a UpstreamErrorResponse(4xx) exception" in {
        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(BAD_REQUEST, Json.toJson(Json.obj()))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

            result shouldBe Left(ComplianceDataUnexpectedFailure(BAD_REQUEST))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }

      "the call returns a UpstreamErrorResponse(5xx) exception" in {
        stubGetComplianceData(testNino, testFromDate, testPoCAchievementDate)(INTERNAL_SERVER_ERROR, Json.toJson(Json.obj()))

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: ComplianceDataResponse = await(connector.getComplianceData(testNino, testFromDate, testPoCAchievementDate)(HeaderCarrier()))

            result shouldBe Left(ComplianceDataUnexpectedFailure(INTERNAL_SERVER_ERROR))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }
    }
  }

}
