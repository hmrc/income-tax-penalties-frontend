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

package utils

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import connectors.PenaltiesConnector.{AppealInformationType, AppealLevelEnum, AppealStatusEnum, BreathingSpace, ExpiryReasonEnum, GetPenaltyDetails, LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPSummary, LatePaymentPenalty, LateSubmission, LateSubmissionPenalty, MainTransactionEnum, TaxReturnStatusEnum, Totalisations}
import play.api.http.Status
import play.api.libs.json.Json

import java.time.LocalDate

/**
 * @see "https://wiremock.org/docs/request-matching/"
 */
trait PenaltiesWiremockStubs {

  val nullPenaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
    totalisations = None, lateSubmissionPenalty = None, latePaymentPenalty = None, breathingSpace = None
  )

  def mockGetPenaltyDetailsResponse(nino: String = "AB123456A", penaltyDetails: Option[GetPenaltyDetails] = None): StubMapping =
    stubFor(get(urlMatching(s"/penalties/etmp/penalties/$nino"))
      .willReturn(aResponse()
        .withStatus(Status.OK)
        .withBody(
          Json.toJson(penaltyDetails.fold(nullPenaltyDetails)(identity)).toString()
        )
      )
    )

}
