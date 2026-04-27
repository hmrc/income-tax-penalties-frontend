/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub
import play.api.http.Status.OK
import play.api.libs.json.Json

class SupplementaryCalculationControllerISpec extends ControllerISpecHelper
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
    val LPP1SupplementaryPath = addQueryParam(pathStart + "additional-first-lpp-calculation")
    val optArn = if (isAgent) Some("123456789") else None


    s"GET $LPP1SupplementaryPath" should {
      setFeatureDate(None)
      "render the supplementary calculation page for LPP1" when {
        "first late payment penalty supplementary charge exists for the penaltyId" in {
          stubAuthRequests(isAgent)
          val supplementary1LPPCalcData = sampleFirstLPPCalcData()
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSupplementaryCalculationPagePage(supplementary1LPPCalcData)))
          val result = get(LPP1SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional first late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional first late payment penalty calculation"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementById("SupplementaryPenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("penaltyStatusPaid").text() shouldBe "To avoid interest charges, you should pay this penalty by 1 May 2026."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("SupplementaryPenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("SupplementaryPenaltyAmountDetailsP2").text() shouldBe "We charge:"
          document.getElementById("SupplementaryPenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
          document.getElementById("SupplementaryPenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
        }
        "first late payment penalty supplementary charge exists for the penaltyId and is overdue" in {
          stubAuthRequests(isAgent)
          val supplementary1LPPCalcData = sampleFirstLPPCalcData(isOverdue = true)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSupplementaryCalculationPagePage(supplementary1LPPCalcData)))
          val result = get(LPP1SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional first late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional first late payment penalty calculation"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementById("SupplementaryPenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("penaltyStatusUnpaid").text() shouldBe "This penalty is overdue. We are charging interest."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("SupplementaryPenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("SupplementaryPenaltyAmountDetailsP2").text() shouldBe "We charge:"
          document.getElementById("SupplementaryPenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
          document.getElementById("SupplementaryPenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
        }
      }
    }
  }
}