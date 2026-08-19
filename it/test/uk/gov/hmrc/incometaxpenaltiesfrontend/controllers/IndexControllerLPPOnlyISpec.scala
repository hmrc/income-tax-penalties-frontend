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
import org.jsoup.select.Elements
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.indexPage.lpp.LPPControllerHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub

import java.time.LocalDate

class IndexControllerLPPOnlyISpec extends LPPControllerHelper with FeatureSwitching
  with PenaltiesStub with PenaltiesDetailsTestData {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  setFeatureDate(None)
  val currentDate: LocalDate = getFeatureDate(appConfig)

  lppUsers.foreach { case (nino, userdetails) =>

    "GET /view-penalty/self-assessment" when {
      "the call to penalties backend returns data" should {
        "render the expected penalty cards" when {
          s"the user with nino $nino is an authorised individual" in {
            val date = userdetails.timeMachineDate match {
              case "now" => currentDate
              case dateText =>
                val tMDate = LocalDate.parse(dateText.replace("/", "-"), timeMachineDateFormatter)
                if (tMDate.isBefore(principleChargeBillingStartDate))
                  throw new IllegalArgumentException(s"timeMachineDate $tMDate is before principalChargeDueDate for nino $nino")
                else
                  tMDate
            }
            setFeatureDate(Some(date))
            stubAuthRequests(false, userdetails.nino)
            stubGetPenalties(userdetails.nino, None)(OK, userdetails.getApiResponseJson(userdetails.nino))
            val result = get("/")
            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyOverview(document, userdetails.expectedOverviewText)
            validatePenaltyTabs(document)
            if (userdetails.numberOfLSPPenalties == 0) {
              validateNoLSPPenalties(document)
            }
            val lppTab = getLPPTabContent(document)
            lppTab.getElementById("lppHeading").text() shouldBe "Late payment penalties"
            lppTab.getElementById("guidanceLatePaymentLink").text() shouldBe "Find out more about late payment penalties (opens in new tab)"
            lppTab.getElementById("guidanceLatePaymentLink").attr("href") shouldBe "https://www.gov.uk/guidance/penalties-for-making-tax-digital-for-income-tax#late-paymentpenalties"

            val lppCards: Elements = lppTab.getElementsByClass("govuk-summary-card")
            lppCards.size() shouldBe userdetails.expectedNumberOfLPPPenaltyCards
            userdetails.validatePenaltyCardsContent(lppCards)
          }

          s"the user is an authorised agent for a client with nino $nino" in {
            val date = userdetails.timeMachineDate match {
              case "now" => currentDate
              case dateText =>
                val tMDate = LocalDate.parse(dateText.replace("/", "-"), timeMachineDateFormatter)
                if (tMDate.isBefore(principleChargeBillingStartDate))
                  throw new IllegalArgumentException(s"timeMachineDate $tMDate is before principalChargeDueDate for nino $nino")
                else
                  tMDate
            }
            setFeatureDate(Some(date))
            stubAuthRequests(true, userdetails.nino)
            stubGetPenalties(userdetails.nino, Some("123456789"))(OK, userdetails.getApiResponseJson(userdetails.nino))
            val result = get("/agent", true)
            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyOverview(document, userdetails.expectedOverviewText)
            validatePenaltyTabs(document)
            if (userdetails.numberOfLSPPenalties == 0) {
              validateNoLSPPenalties(document, true)
            }
            val lppTab = getLPPTabContent(document)
            lppTab.getElementById("lppHeading").text() shouldBe "Late payment penalties"
            val lppCards: Elements = lppTab.getElementsByClass("govuk-summary-card")
            lppCards.size() shouldBe userdetails.expectedNumberOfLPPPenaltyCards
            userdetails.validatePenaltyCardsContent(lppCards)
          }
        }
      }
    }
  }

  "GET /view-penalty/self-assessment" when {
    "the call to penalties backend returns data" should {
      "show the paid penalty intro text for a paid LPP" in {
        val userdetails = lppUsers("AA100000C")
        val date = LocalDate.parse(userdetails.timeMachineDate.replace("/", "-"), timeMachineDateFormatter)
        setFeatureDate(Some(date))
        stubAuthRequests(false, userdetails.nino)
        stubGetPenalties(userdetails.nino, None)(OK, userdetails.getApiResponseJson(userdetails.nino))

        val document = Jsoup.parse(get("/").body)
        val lppTab = getLPPTabContent(document)

        lppTab.getElementsByClass("govuk-body").first().text() shouldBe "You do not have any late payment penalties."
      }

      "show the tax paid but penalty not paid text for a due LPP" in {
        val userdetails = lppUsers("AA100000B")
        val date = LocalDate.parse(userdetails.timeMachineDate.replace("/", "-"), timeMachineDateFormatter)
        setFeatureDate(Some(date))
        stubAuthRequests(false, userdetails.nino)
        stubGetPenalties(userdetails.nino, None)(OK, userdetails.getApiResponseJson(userdetails.nino))

        val document = Jsoup.parse(get("/").body)
        val lppTab = getLPPTabContent(document)
        val lppBodies = lppTab.getElementsByClass("govuk-body")

        lppBodies.get(0).text() shouldBe "You can pay your penalty now."
        lppBodies.get(1).text().replace("'", "").replace("’", "") shouldBe "Your penalties are no longer estimates because you've paid your outstanding tax.".replace("'", "").replace("’", "")
      }

      "show the default pay-early info text when the penalty is unpaid and the tax is unpaid" in {
        val userdetails = lppUsers("AA100000A")
        val date = LocalDate.parse(userdetails.timeMachineDate.replace("/", "-"), timeMachineDateFormatter)
        setFeatureDate(Some(date))
        stubAuthRequests(false, userdetails.nino)
        stubGetPenalties(userdetails.nino, None)(OK, userdetails.getApiResponseJson(userdetails.nino))

        val document = Jsoup.parse(get("/").body)
        val lppTab = getLPPTabContent(document)

        lppTab.getElementsByClass("govuk-body").first().text() shouldBe "The earlier you pay your Income Tax, the less you’ll pay in penalties and interest."
      }
    }
  }
}
