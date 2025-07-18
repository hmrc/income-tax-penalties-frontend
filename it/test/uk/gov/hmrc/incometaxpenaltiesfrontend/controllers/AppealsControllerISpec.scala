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

class AppealsControllerISpec extends ControllerISpecHelper {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "GET /appeal-penalty" should {

    "redirect the individual to the appeals service when the penalty is a LSP" in {
      stubAuthRequests(false)

      val result = get("/appeal-penalty", queryParams = Map("penaltyId" -> "1234"))

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=false&isLPP=false&isAdditional=false&is2ndStageAppeal=false")
    }

    "redirect the individual to the appeals service when the penalty is a LSP and is a 2nd Stage Appeal" in {
      stubAuthRequests(false)

      val result = get("/appeal-penalty", queryParams = Map("penaltyId" -> "1234", "is2ndStageAppeal" -> "true"))

      result.status shouldBe SEE_OTHER
      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=false&isLPP=false&isAdditional=false&is2ndStageAppeal=true")
    }

    "redirect the individual to the appeals service when the penalty is a LPP1" in {
      stubAuthRequests(false)
      val result = get("/appeal-penalty", queryParams = Map("penaltyId" -> "1234", "isLPP" -> "true"))

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=false&isLPP=true&isAdditional=false&is2ndStageAppeal=false")
    }

    "redirect the individual to the appeals service when the penalty is a LPP2" in {
      stubAuthRequests(false)
      val result = get("/appeal-penalty", queryParams = Map("penaltyId" -> "1234", "isLPP" -> "true", "isLPP2" -> "true"))

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=false&isLPP=true&isAdditional=true&is2ndStageAppeal=false")
    }

    "redirect the individual to the obligations appeals service when the penalty is a LSP" in {
      stubAuthRequests(false)
      val result = get("/appeal-penalty", queryParams = Map("penaltyId" -> "1234", "isFindOutHowToAppealLSP" -> "true"))

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=false&isLPP=false&isAdditional=false&is2ndStageAppeal=false")
    }
  }

  "GET /agent-appeal-penalty" should {

    "redirect the agent to the appeals service when the penalty is a LSP" in {
      stubAuthRequests(true)
      val result = get("/agent-appeal-penalty", queryParams = Map("penaltyId" -> "1234"), isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=true&isLPP=false&isAdditional=false&is2ndStageAppeal=false")
    }

    "redirect the agent to the appeals service when the penalty is a LPP1" in {
      stubAuthRequests(true)
      val result = get("/agent-appeal-penalty", queryParams = Map("penaltyId" -> "1234", "isLPP" -> "true"), isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=true&isLPP=true&isAdditional=false&is2ndStageAppeal=false")
    }

    "redirect the agent to the appeals service when the penalty is a LPP2" in {
      stubAuthRequests(true)
      val result = get("/agent-appeal-penalty", queryParams = Map("penaltyId" -> "1234", "isLPP" -> "true", "isLPP2" -> "true"), isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=true&isLPP=true&isAdditional=true&is2ndStageAppeal=false")
    }

    "redirect the agent to the obligations appeals service when the penalty is a LSP" in {
      stubAuthRequests(true)
      val result = get("/agent-appeal-penalty", queryParams = Map("penaltyId" -> "1234", "isFindOutHowToAppealLSP" -> "true"), isAgent = true)

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal?penaltyId=1234&isAgent=true&isLPP=false&isAdditional=false&is2ndStageAppeal=false")
    }
  }

  "Get /find-out-how-to-appeal" should {

    "redirect the individual to the find-out-how-to-appeal page" in {
      stubAuthRequests(false)
      val result = get("/find-out-how-to-appeal", queryParams = Map("principalChargeReference" -> "12345678901234", "itsaAmountInPence" -> "2000", "itsaPeriodStartDate" -> "11/11/22", "itsaPeriodEndDate" -> "22/11/22"))

      result.status shouldBe SEE_OTHER

      result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal-find-out-how-to-appeal?principalChargeReference=12345678901234&isAgent=false&itsaAmountInPence=2000&itsaPeriodStartDate=11/11/22&itsaPeriodEndDate=22/11/22")
    }

    "Get /agent-find-out-how-to-appeal" should {

      "redirect the agent to the find-out-how-to-appeal page" in {
        stubAuthRequests(true)
        val result = get("/agent-find-out-how-to-appeal", queryParams = Map("principalChargeReference" -> "12345678901234", "itsaAmountInPence" -> "2000", "itsaPeriodStartDate" -> "11/11/22", "itsaPeriodEndDate" -> "22/11/22"), isAgent = true)

        result.status shouldBe SEE_OTHER

        result.headers(HeaderNames.LOCATION) shouldBe List("http://localhost:9188/appeal-penalty/self-assessment/initialise-appeal-find-out-how-to-appeal?principalChargeReference=12345678901234&isAgent=true&itsaAmountInPence=2000&itsaPeriodStartDate=11/11/22&itsaPeriodEndDate=22/11/22")
      }

    }
  }
}
