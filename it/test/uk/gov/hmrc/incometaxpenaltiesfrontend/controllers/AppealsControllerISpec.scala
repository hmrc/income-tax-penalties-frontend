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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import play.api.http.HeaderNames
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.ComponentSpecHelper

class AppealsControllerISpec extends ComponentSpecHelper with AuthStub {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "GET /appeal-penalty" should {
    "redirect the individual to the appeals service when the penalty is a LSP" in {
      stubAuth(OK, successfulIndividualAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234")

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal?penaltyId=1234&isLPP=false&isAdditional=false"
    }

    "redirect the individual to the appeals service when the penalty is a LPP1" in {
      stubAuth(OK, successfulIndividualAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234&isLPP=true")

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=false"
    }

    "redirect the individual to the appeals service when the penalty is a LPP2" in {
      stubAuth(OK, successfulIndividualAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234&isLPP=true&isLPP2=true")

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=true"
    }

    "redirect the individual to the obligations appeals service when the penalty is a LSP" in {
      stubAuth(OK, successfulIndividualAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234&isFindOutHowToAppealLSP=true")

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal-against-the-obligation?penaltyId=1234"
    }

    "redirect the agent to the appeals service when the penalty is a LSP" in {
      stubAuth(OK, successfulAgentAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234", isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal?penaltyId=1234&isLPP=false&isAdditional=false"
    }

    "redirect the agent to the appeals service when the penalty is a LPP1" in {
      stubAuth(OK, successfulAgentAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234&isLPP=true", isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=false"
    }

    "redirect the agent to the appeals service when the penalty is a LPP2" in {
      stubAuth(OK, successfulAgentAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234&isLPP=true&isLPP2=true", isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal?penaltyId=1234&isLPP=true&isAdditional=true"
    }

    "redirect the agent to the obligations appeals service when the penalty is a LSP" in {
      stubAuth(OK, successfulAgentAuthResponse)
      val result = get("/appeal-penalty\\?penaltyId=1234&isFindOutHowToAppealLSP=true", isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe "http://localhost:9188/penalties-appeals/income-tax/initialise-appeal-against-the-obligation?penaltyId=1234"
    }
  }
}