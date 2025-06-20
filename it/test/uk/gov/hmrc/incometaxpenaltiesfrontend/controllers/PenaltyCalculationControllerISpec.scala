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

import fixtures.PenaltiesDetailsTestData
import org.jsoup.Jsoup
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub

class PenaltyCalculationControllerISpec extends ControllerISpecHelper
  with PenaltiesStub with PenaltiesDetailsTestData with FeatureSwitching {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  def addQueryParam(pathNoQuery: String): String = {
    pathNoQuery + "?penaltyId=" + principleChargeRef
  }

  List(false, true).foreach { isAgent =>
    val pathStart = if (isAgent) "/agent-" else "/"
    val firstLPPPath = addQueryParam(pathStart + "first-lpp-calculation")
    val secondLPPPath = addQueryParam(pathStart + "second-lpp-calculation")
    val optArn = if(isAgent) Some("123456789") else None
    s"GET $firstLPPPath" when {
      "a first late payment penalty exists for the penaltyId" should {
        "render the first late payment calculation page" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(samplePenaltyDetailsModel))

          val result = get(firstLPPPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "First late payment penalty calculation"
          document.getParagraphs.get(0).text() shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
          document.getParagraphs.get(1).text() shouldBe "It is made up of 2 parts:"
          document.getBulletPoints.get(0).text() shouldBe "2% of £20,000 (the unpaid Income Tax 15 days after the due date)"
          document.getBulletPoints.get(1).text() shouldBe "2% of £20,000 (the unpaid Income Tax 30 days after the due date)"
          document.getSummaryListQuestion.get(0).text() shouldBe "Penalty amount"
          document.getSummaryListQuestion.get(1).text() shouldBe "Amount received"
          document.getSummaryListQuestion.get(2).text() shouldBe "Left to pay"
          document.getSummaryListAnswer.get(0).text() shouldBe "£800.00"
          document.getSummaryListAnswer.get(1).text() shouldBe "£800.00"
          document.getSummaryListAnswer.get(2).text() shouldBe "£0.00"
          document.getLink("returnToIndex").text() shouldBe "Return to Self Assessment penalties and appeals"
        }
      }

      "a penalty does not exist for the penaltyId" should {
        "redirect to penalties home" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(emptyPenaltyDetailsModel))

          val result = get(firstLPPPath, isAgent)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.IndexController.homePage(isAgent).url)
        }
      }
    }

    s"GET $secondLPPPath" when {
      "a second late payment penalty exists for the penaltyId" should {
        "render the second late payment calculation page" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(samplePenaltyDetailsLPP2Model))

          val result = get(secondLPPPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
          document.getParagraphs.get(0).text() shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
          document.getParagraphs.get(1).text() shouldBe "It is made up of 2 parts:"
          document.getBulletPoints.get(0).text() shouldBe "2% of £20,000 (the unpaid Income Tax 15 days after the due date)"
          document.getBulletPoints.get(1).text() shouldBe "2% of £20,000 (the unpaid Income Tax 30 days after the due date)"
          document.getSummaryListQuestion.get(0).text() shouldBe "Penalty amount"
          document.getSummaryListQuestion.get(1).text() shouldBe "Amount received"
          document.getSummaryListQuestion.get(2).text() shouldBe "Left to pay"
          document.getSummaryListAnswer.get(0).text() shouldBe "£800.00"
          document.getSummaryListAnswer.get(1).text() shouldBe "£800.00"
          document.getSummaryListAnswer.get(2).text() shouldBe "£0.00"
          document.getLink("returnToIndex").text() shouldBe "Return to Self Assessment penalties and appeals"
        }
      }

      "a penalty does not exist for the penaltyId" should {
        "redirect to penalties home" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(emptyPenaltyDetailsModel))

          val result = get(firstLPPPath, isAgent)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.IndexController.homePage(isAgent).url)
        }
      }
    }
  }
}
