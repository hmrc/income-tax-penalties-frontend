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

package uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ComplianceData
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper.PagerDutyKeys._

object ComplianceDataParser {

  sealed trait GetComplianceDataFailure {
    val message: String
  }
  case class ComplianceDataUnexpectedFailure(status: Int) extends GetComplianceDataFailure {
    override val message: String = s"Received status code: $status"
  }
  case object ComplianceDataNoData extends GetComplianceDataFailure {
    override val message: String = "Received no data from call"
  }
  case object ComplianceDataMalformed extends GetComplianceDataFailure {
    override val message: String = "Body received was malformed"
  }

  type ComplianceDataResponse = Either[GetComplianceDataFailure, ComplianceData]

  implicit object ComplianceDataReads extends HttpReads[ComplianceDataResponse] {
    override def read(method: String, url: String, response: HttpResponse): ComplianceDataResponse = {
      response.status match {
        case OK =>
          logger.debug(s"[ComplianceDataReads][read]: Successful call to retrieve Compliance Data${response.json}")
          response.json.validate[ComplianceData](ComplianceData.format) match {
            case JsSuccess(compliancePayload, _) =>
              Right(compliancePayload)

            case JsError(errors) =>
              PagerDutyHelper.log("ComplianceDataReads", INVALID_JSON_RECEIVED_FROM_PENALTIES_BACKEND)
              logger.debug(s"[ComplianceDataReads][read]: Failed to parse to model - failures: $errors")
              logger.error("[ComplianceDataReads][read]: Failed to parse to model")

              Left(ComplianceDataMalformed)
          }

        case NOT_FOUND =>
          logger.info(s"[ComplianceDataReads][read] - Received not found response from backend for API 1330 data." +
            s" No data associated with VRN. Body: ${response.body}")
          Left(ComplianceDataNoData)

        case BAD_REQUEST =>
          PagerDutyHelper.log("ComplianceDataReads", RECEIVED_4XX_FROM_PENALTIES_BACKEND)
          logger.error(s"[ComplianceDataReads][read] - Failed to parse to model with response body: ${response.body} (Status: $BAD_REQUEST)")
          Left(ComplianceDataUnexpectedFailure(BAD_REQUEST))

        case status =>
          PagerDutyHelper.logStatusCode("ComplianceDataReads", status)(RECEIVED_4XX_FROM_PENALTIES_BACKEND, RECEIVED_5XX_FROM_PENALTIES_BACKEND)
          logger.error(s"[ComplianceDataReads][read] Received unexpected response when calling backend for API 1330 data," +
            s" status code: $status and body: ${response.body}")
          Left(ComplianceDataUnexpectedFailure(status))
      }
    }
  }
}

