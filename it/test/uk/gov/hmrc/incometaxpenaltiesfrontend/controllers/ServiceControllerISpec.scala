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

import org.jsoup.Jsoup
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

import java.net.URLEncoder

class ServiceControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub {

  "GET /" should {
    "return an OK with a view" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/")

        result.status shouldBe OK
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/", isAgent = true)

        result.status shouldBe OK
      }
    }
    "have the correct page has correct elements" when {
      "the user is an authorised individual" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/")

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
        document.getH2Elements.get(0).text() shouldBe "Overview"
        document.getParagraphs.get(0).text() shouldBe "Your account has:"
        document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
        document.getH3Elements.get(0).text() shouldBe "Late submission penalties"
        document.getH3Elements.get(1).text() shouldBe "Late payment penalties"
        document.getElementById("financialWarningText").text() shouldBe "! Warning You have reached the financial penalty threshold."
        document.getSubmitButton.text() shouldBe "Check amounts and pay"
        document.getLink("actionsToRemoveLink").text() shouldBe "Actions to take to get your points removed (opens in new tab)"
        document.getLink("actionsToRemoveLink").attr("href") shouldBe "/penalties/income-tax/compliance-timeline"
        document.getLink("appealPenalty1Link").attr("href") shouldBe "/penalties/income-tax/appeal-penalty?penaltyId=1"
        document.getLink("appealPenaltyPoint1Link").attr("href") shouldBe "/penalties/income-tax/appeal-penalty?penaltyId=1"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
        document.getH2Elements.get(0).text() shouldBe "Overview"
        document.getParagraphs.get(0).text() shouldBe "Your client's account has:"
        document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
        document.getH3Elements.get(0).text() shouldBe "Late submission penalties"
        document.getH3Elements.get(1).text() shouldBe "Late payment penalties"
        document.getElementById("financialWarningText").text() shouldBe "! Warning Your client has been given a £200 penalty for reaching the penalty threshold."
        document.getSubmitButton.text() shouldBe "Check amounts and pay"
        document.getLink("actionsToRemoveLink").text() shouldBe "Actions your client must take to get their points removed (opens in new tab)"
        document.getLink("actionsToRemoveLink").attr("href") shouldBe "/penalties/income-tax/compliance-timeline"
        document.getLink("appealPenalty1Link").attr("href") shouldBe "/penalties/income-tax/appeal-penalty?penaltyId=1"
        document.getLink("appealPenaltyPoint1Link").attr("href") shouldBe "/penalties/income-tax/appeal-penalty?penaltyId=1"
      }
    }
  }

  "GET /penalties/income-tax/sign-out" should {
    "redirect to sign-out route with the continue URL set to the feedback survey" in {
      val appConfig = app.injector.instanceOf[AppConfig]
      stubAuth(OK, successfulIndividualAuthResponse)

      val result = get("/sign-out")

      val encodedContinueUrl = URLEncoder.encode(appConfig.survey, "UTF-8")
      val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

      result.status shouldBe SEE_OTHER
      result.header(LOCATION) shouldBe Some(expectedRedirectUrl)
    }
  }

  "GET /penalties/income-tax/keep-alive" should {
    "return No-Content" in {
      stubAuth(OK, successfulIndividualAuthResponse)

      val result = get("/keep-alive")
      result.status shouldBe NO_CONTENT
    }
  }
}
