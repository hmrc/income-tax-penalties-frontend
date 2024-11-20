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

package uk.gov.hmrc.incometaxpenaltiesfrontend.connectors


import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import javax.inject.Inject
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.User
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.featureSwitches.FeatureSwitching
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.{GetPenaltyDetailsResponse, GetPenaltyDetailsResponseReads}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.ComplianceDataParser.{CompliancePayloadFailureResponse, CompliancePayloadResponse}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.Logger.logger
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.PagerDutyHelper.PagerDutyKeys._
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.UnexpectedFailure
import play.api.http.Status.INTERNAL_SERVER_ERROR
import java.time.LocalDate

class PenaltiesConnector @Inject()(httpClient: HttpClient,
                                   val appConfig: AppConfig)(implicit ec: ExecutionContext) extends FeatureSwitching {


  private val penaltiesBaseUrl: String = appConfig.penaltiesUrl

  private def getPenaltiesDataUrl(enrolmentKey: String)(implicit user: User[_]): String = {
    val urlQueryParams = user.arn.fold("")(arn => s"?arn=$arn")
    s"/etmp/penalties/$enrolmentKey$urlQueryParams"
  }

  private def getDESObligationsDataUrl(vrn: String, fromDate: String, toDate: String): String =
    s"/compliance/des/compliance-data?vrn=$vrn&fromDate=$fromDate&toDate=$toDate"

  def getPenaltyDetails(enrolmentKey: String)(implicit user: User[_], hc: HeaderCarrier): Future[GetPenaltyDetailsResponse] = {
    logger.info(s"[PenaltiesConnector][getPenaltyDetails] - Requesting penalties details from backend for VRN $enrolmentKey.")
    httpClient.GET[GetPenaltyDetailsResponse](s"$penaltiesBaseUrl${getPenaltiesDataUrl(enrolmentKey)}")(GetPenaltyDetailsResponseReads, hc, ec).recover{
      case e: UpstreamErrorResponse =>
        PagerDutyHelper.logStatusCode("PenaltiesConnector: getPenaltyDetails", e.statusCode)(
          RECEIVED_4XX_FROM_PENALTIES_BACKEND, RECEIVED_5XX_FROM_PENALTIES_BACKEND)
        logger.error(s"[PenaltiesConnector][getPenaltyDetails] - Received ${e.statusCode} status from Penalties backend call - returning status to caller")
        Left(UnexpectedFailure(e.statusCode, e.getMessage))
      case e: Exception =>
        PagerDutyHelper.log("PenaltiesConnector: getPenaltyDetails", UNEXPECTED_ERROR_FROM_PENALTIES_BACKEND)
        logger.error(s"[PenaltiesConnector][getPenaltyDetails] - An unknown exception occurred - returning 500 back to caller - message: ${e.getMessage}")
        Left(UnexpectedFailure(INTERNAL_SERVER_ERROR, e.getMessage))
    }
  }

  def getObligationData(vrn: String, fromDate: LocalDate, toDate: LocalDate)(implicit hc: HeaderCarrier): Future[CompliancePayloadResponse] = {
    logger.info(s"[PenaltiesConnector][getObligationData] - Requesting obligation data from backend for VRN $vrn.")
    httpClient.GET[CompliancePayloadResponse](s"$penaltiesBaseUrl${getDESObligationsDataUrl(vrn, fromDate.toString, toDate.toString)}").recover {
      case e: UpstreamErrorResponse => {
        PagerDutyHelper.logStatusCode("getObligationData", e.statusCode)(RECEIVED_4XX_FROM_PENALTIES_BACKEND, RECEIVED_5XX_FROM_PENALTIES_BACKEND)
        logger.error(s"[PenaltiesConnector][getObligationData] -" +
          s" Received ${e.statusCode} status from API 1330 call - returning status to caller")
        Left(CompliancePayloadFailureResponse(e.statusCode))
      }
      case e: Exception => {
        PagerDutyHelper.log("getObligationData", UNEXPECTED_ERROR_FROM_PENALTIES_BACKEND)
        logger.error(s"[PenaltiesConnector][getObligationData] -" +
          s" An unknown exception occurred - returning 500 back to caller - message: ${e.getMessage}")
        Left(CompliancePayloadFailureResponse(INTERNAL_SERVER_ERROR))
      }
    }
  }
}