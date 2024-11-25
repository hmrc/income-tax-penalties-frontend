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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.PenaltyDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper
import PagerDutyHelper.PagerDutyKeys._

object GetPenaltyDetailsParser {

  sealed trait GetPenaltyDetailsFailure {
    val message: String
  }

  case class GetPenaltyDetailsUnexpectedFailure(status: Int) extends GetPenaltyDetailsFailure {
    override val message: String = s"Unexpected response, status $status returned"
  }

  case object GetPenaltyDetailsBadRequest extends GetPenaltyDetailsFailure {
    override val message: String = "Sent a bad request to downstream service"
  }

  case object GetPenaltyDetailsMalformed extends GetPenaltyDetailsFailure {
    override val message: String = "Body received was malformed"
  }


  type GetPenaltyDetailsResponse = Either[GetPenaltyDetailsFailure, PenaltyDetails]

  implicit object GetPenaltyDetailsResponseReads extends HttpReads[GetPenaltyDetailsResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetPenaltyDetailsResponse = {
      response.status match {
        case OK =>
          response.json.validate[PenaltyDetails] match {
            case JsSuccess(model, _) =>
              logger.info("[GetPenaltyDetailsResponseReads][read]: Successful call to retrieve penalties details.")
              Right(model)

            case JsError(errors) =>
              logger.debug(s"[GetPenaltyDetailsResponseReads][read]: Failed to parse to model - failures: $errors")
              logger.error("[GetPenaltyDetailsResponseReads][read]: Failed to parse to model")
              PagerDutyHelper.log("PenaltiesConnectorParser: GetPenaltyDetailsResponseReads", INVALID_JSON_RECEIVED_FROM_PENALTIES_BACKEND)
              Left(GetPenaltyDetailsMalformed)
          }

        case NO_CONTENT =>
          logger.info(s"[GetPenaltyDetailsResponseReads][read]: No content found for MTDITID provided, returning empty model")
          Right(PenaltyDetails(None, None, None, None))

        case BAD_REQUEST =>
          logger.error(s"[GetPenaltyDetailsResponseReads][read]: Bad request returned with reason: ${response.body}")
          PagerDutyHelper.log("PenaltiesConnectorParser: GetPenaltyDetailsResponseReads", RECEIVED_4XX_FROM_PENALTIES_BACKEND)
          Left(GetPenaltyDetailsBadRequest)

        case status =>
          logger.error(s"[GetPenaltyDetailsResponseReads][read]: Unexpected response, status $status returned with reason: ${response.body}")
          PagerDutyHelper.logStatusCode("PenaltiesConnectorParser: GetPenaltyDetailsResponseReads", status)(
            RECEIVED_4XX_FROM_PENALTIES_BACKEND, RECEIVED_5XX_FROM_PENALTIES_BACKEND)
          Left(GetPenaltyDetailsUnexpectedFailure(status))
      }
    }
  }
}
