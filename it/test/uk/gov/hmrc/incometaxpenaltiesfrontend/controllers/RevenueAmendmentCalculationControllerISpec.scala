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
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub

import java.time.LocalDate

class RevenueAmendmentCalculationControllerISpec extends ControllerISpecHelper
  with PenaltiesStub with PenaltiesDetailsTestData with FeatureSwitching {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  private def calculationPath(isAgent: Boolean): String = {
    val pathStart = if (isAgent) "/agent-" else "/"
    s"${pathStart}first-lpp-calculation?penaltyId=$principleChargeRef"
  }

  private def getDateString(date: LocalDate): String =
    s"${date.getDayOfMonth} ${messagesAPI(s"month.${date.getMonthValue}")} ${date.getYear}"

  private def revenueAmendmentPenaltyDetails(isOverdue: Boolean) = {
    val calculationData = sampleFirstLPPCalcData(isIncomeTaxPaid = true, isEstimate = false, isOverdue = isOverdue)
    val response = getPenaltyDetailsForCalculationPage(calculationData)
    val revenueAmendmentResponse = response.copy(
      penaltyDetails = response.penaltyDetails.map { details =>
        val lppDetails = details.latePaymentPenalty.get
        details.copy(
          latePaymentPenalty = Some(lppDetails.copy(
            lppDetails = Some(lppDetails.details.map(_.copy(chargeClassificationValue = Some("RA"))))
          ))
        )
      }
    )
    (calculationData, revenueAmendmentResponse)
  }

  List(false, true).foreach { isAgent =>
    val path = calculationPath(isAgent)
    val optArn = if (isAgent) Some("123456789") else None

    s"GET $path for a revenue amendment" should {
      setFeatureDate(None)

      "render the pay-by and interest-rate details when the penalty is not overdue" in {
        stubAuthRequests(isAgent)
        val (calculationData, response) = revenueAmendmentPenaltyDetails(isOverdue = false)
        stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(response))

        val result = get(path, isAgent)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.getElementById("penaltyDetailsDuePayBy").text() shouldBe
          s"Pay by ${getDateString(calculationData.payPenaltyBy)} to stop interest from being added to your penalty."
        document.getElementById("penaltyDetailsDueMoreDetails").text() shouldBe
          "Find out more about HMRC interest rates (opens in new tab)."
        val interestRatesLink = document.select("#penaltyDetailsDueMoreDetails a")
        interestRatesLink.attr("href") shouldBe appConfig.hmrcInterestRatesUrl
        interestRatesLink.attr("target") shouldBe "_blank"
        document.getElementById("penaltyDetailsOverdueMoreDetails") shouldBe null
      }

      "render the interest-charged and interest-rate details when the penalty is overdue" in {
        stubAuthRequests(isAgent)
        val (_, response) = revenueAmendmentPenaltyDetails(isOverdue = true)
        stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(response))

        val result = get(path, isAgent)
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)
        document.getElementById("penaltyDetailsExtraTextOverdue").text() shouldBe
          "This penalty is overdue. We are charging interest."
        document.getElementById("penaltyDetailsExtraTextOverdueInterestCharged").text() shouldBe
          "Interest is charged separately and is not included in the penalty amount. You’ll see any interest due when you check what you owe and pay."

        val checkWhatYouOweLink = document.select("#penaltyDetailsExtraTextOverdueInterestCharged a")
        checkWhatYouOweLink.attr("href") shouldBe appConfig.checkWhatYouOweUrl(isAgent)
        checkWhatYouOweLink.text() shouldBe "check what you owe and pay."

        document.getElementById("penaltyDetailsOverdueMoreDetails").text() shouldBe
          "Find out more about HMRC interest rates (opens in new tab)."
        val interestRatesLink = document.select("#penaltyDetailsOverdueMoreDetails a")
        interestRatesLink.attr("href") shouldBe appConfig.hmrcInterestRatesUrl
        interestRatesLink.attr("target") shouldBe "_blank"

        document.select("#checkWhatYouOweLink").isEmpty shouldBe true
      }
    }
  }
}
