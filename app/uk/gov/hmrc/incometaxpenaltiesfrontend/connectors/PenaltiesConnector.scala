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


import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.ComplianceDataParser.ComplianceDataResponse
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.GetPenaltyDetailsResponse
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.incometaxpenaltiesfrontend.constants.Logger.logger

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PenaltiesConnector @Inject()(httpClient: HttpClientV2,
                                   val appConfig: AppConfig
                                  )(implicit ec: ExecutionContext) extends FeatureSwitching {

  def getPenaltyDetails(enrolmentKey: String, optArn: Option[String] = None)
                       (implicit hc: HeaderCarrier): Future[GetPenaltyDetailsResponse] = {
    logger.info(s"[PenaltiesConnector][getPenaltyDetails] - Requesting penalties details from backend for VRN $enrolmentKey.")

    httpClient
      .get(url"${appConfig.penaltiesUrl + s"/etmp/penalties/$enrolmentKey${optArn.map(arn => s"?arn=$arn").getOrElse("")}"}")
      .execute[GetPenaltyDetailsResponse]
  }

  def getObligationData(mtdItId: String, fromDate: LocalDate, toDate: LocalDate)
                       (implicit hc: HeaderCarrier): Future[ComplianceDataResponse] = {
    logger.info(s"[PenaltiesConnector][getObligationData] - Requesting obligation data from backend for MTDITID $mtdItId.")

    httpClient
      .get(url"${appConfig.penaltiesUrl + s"/compliance/des/compliance-data?mtditid=$mtdItId&fromDate=$fromDate&toDate=$toDate"}")
      .execute[ComplianceDataResponse]
  }

}
