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
import uk.gov.hmrc.incometaxpenaltiesfrontend.constants.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.constants.PagerDutyHelper.PagerDutyKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.fixtures.PenaltiesFixture
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.PenaltyDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, WiremockMethods}
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

class PenaltiesConnectorISpec extends ComponentSpecHelper with LogCapturing with WiremockMethods with PenaltiesFixture{

  val connector: PenaltiesConnector = app.injector.instanceOf[PenaltiesConnector]

  "getPenaltyDetails" should {
    "return a successful response when the call succeeds and the body can be parsed" when {
      "an individual has Late Payment Metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId").thenReturn(status = OK, body = samplePenaltyDetailsModel)

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(testMtdItId)(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "a client has Late Payment Metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId\\?arn=$testArn").thenReturn(status = OK, body = samplePenaltyDetailsModel)

        val result: GetPenaltyDetailsResponse = await(connector.getPenaltyDetails(testMtdItId, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModel)
      }

      "an individual does not have metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId").thenReturn(status = OK, body = samplePenaltyDetailsModelWithoutMetadata)

        val result = await(connector.getPenaltyDetails(testMtdItId)(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }

      "a client does not have metadata" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId\\?arn=$testArn").thenReturn(status = OK, body = samplePenaltyDetailsModelWithoutMetadata)

        val result = await(connector.getPenaltyDetails(testMtdItId, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(samplePenaltyDetailsModelWithoutMetadata)
      }

      "an individual does not have any penalty details" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId").thenReturn(status = NO_CONTENT, body = Json.obj())

        val result = await(connector.getPenaltyDetails(testMtdItId)(HeaderCarrier()))

        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }

      "a client does not have any penalty details" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId\\?arn=$testArn").thenReturn(status = NO_CONTENT, body = Json.obj())

        val result = await(connector.getPenaltyDetails(testMtdItId, Some(testArn))(HeaderCarrier()))

        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }
    }

    "return a Left when" when {
      "an exception with status 4xx occurs upstream" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId").thenReturn(status = BAD_REQUEST, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails(testMtdItId)(HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result shouldBe Left(GetPenaltyDetailsBadRequest)
          }
        }
      }

      "invalid Json is returned" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId").thenReturn(status = OK, body = "")

        val result = await(connector.getPenaltyDetails(testMtdItId)(HeaderCarrier()))

        result shouldBe Left(GetPenaltyDetailsMalformed)
      }

      "an exception with status 5xx occurs upstream is returned" in {
        when(GET, uri = s"/penalties/etmp/penalties/$testMtdItId").thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result = await(connector.getPenaltyDetails(testMtdItId)(HeaderCarrier()))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
            result shouldBe Left(GetPenaltyDetailsUnexpectedFailure(INTERNAL_SERVER_ERROR))
          }
        }
      }
    }
  }

  "getObligationData" should {
    s"return a successful response when the call succeeds and the body can be parsed" in {
      when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtdItId&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
        .thenReturn(status = OK, body = sampleComplianceData)

      val result: ComplianceDataResponse = await(connector.getObligationData(testMtdItId, testFromDate, testToDate)(HeaderCarrier()))

      result shouldBe Right(sampleComplianceData)
    }

    "return a Left response" when {
      "the call returns a OK response however the body is not parsable as a model" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtdItId&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = OK, body = Json.obj("invalid" -> "json"))

        val result: ComplianceDataResponse = await(connector.getObligationData(testMtdItId, testFromDate, testToDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataMalformed)
      }

      "the call returns a Not Found status" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtdItId&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = NOT_FOUND, body = Json.obj())

        val result: ComplianceDataResponse = await(connector.getObligationData(testMtdItId, testFromDate, testToDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataNoData)
      }

      "the call returns an unmatched response" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtdItId&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = SERVICE_UNAVAILABLE, body = Json.obj())

        val result: ComplianceDataResponse = await(connector.getObligationData(testMtdItId, testFromDate, testToDate)(HeaderCarrier()))

        result shouldBe Left(ComplianceDataUnexpectedFailure(SERVICE_UNAVAILABLE))
      }

      "the call returns a UpstreamErrorResponse(4xx) exception" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtdItId&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = BAD_REQUEST, body = Json.obj())


        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: ComplianceDataResponse = await(connector.getObligationData(testMtdItId, testFromDate, testToDate)(HeaderCarrier()))

            result shouldBe Left(ComplianceDataUnexpectedFailure(BAD_REQUEST))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_4XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }

      "the call returns a UpstreamErrorResponse(5xx) exception" in {
        when(GET, uri = s"/penalties/compliance/des/compliance-data\\?mtditid=$testMtdItId&fromDate=${testFromDate.toString}&toDate=${testToDate.toString}")
          .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj())

        withCaptureOfLoggingFrom(logger) {
          logs => {
            val result: ComplianceDataResponse = await(connector.getObligationData(testMtdItId, testFromDate, testToDate)(HeaderCarrier()))

            result shouldBe Left(ComplianceDataUnexpectedFailure(INTERNAL_SERVER_ERROR))
            logs.exists(_.getMessage.contains(PagerDutyKeys.RECEIVED_5XX_FROM_PENALTIES_BACKEND.toString)) shouldBe true
          }
        }
      }
    }
  }

}
