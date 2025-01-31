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

package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.GetPenaltyDetailsResponse
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.CurrentUserRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.EnrolmentUtil

import javax.inject.Inject
import scala.concurrent.Future

class PenaltiesService @Inject()(connector: PenaltiesConnector) {

  def getPenaltyDataForUser()(implicit user: CurrentUserRequest[_], hc: HeaderCarrier): Future[GetPenaltyDetailsResponse] =
    connector.getPenaltyDetails(EnrolmentUtil.constructMTDITEnrolmentKey(user.mtdItId), user.arn)

}

