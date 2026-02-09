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

  val defaultTimeMachineDate: LocalDate = getFeatureDate(appConfig)

  lppUsers.foreach { case (nino, userdetails) =>

    "GET /view-penalty/self-assessment" when {
      "the call to penalties backend returns data" should {
        "render the expected penalty cards" when {
          val date: LocalDate = userdetails.timeMachineDate.map(date =>
            LocalDate.parse(date.replace("/", "-"), timeMachineDateFormatter)
          ).getOrElse {
            defaultTimeMachineDate
          }

          s"the user with nino $nino is an authorised individual" in {
            setFeatureDate(Some(date))
            stubAuthRequests(false, userdetails.nino)
            stubGetPenalties(userdetails.nino, None)(OK, userdetails.getApiResponseJson(userdetails.nino))
            val result = get("/")
            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyOverview(document, userdetails.expectedOverviewText(false))
            validatePenaltyTabs(document)
            if (userdetails.numberOfLSPPenalties == 0) {
              validateNoLSPPenalties(document)
            }
            val lppTab = getLPPTabContent(document)
            lppTab.getElementById("lppHeading").text() shouldBe "Late payment penalties"
            lppTab.getElementsByClass("govuk-body").first().text() shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
            val lppCards: Elements = lppTab.getElementsByClass("govuk-summary-card")
            lppCards.size() shouldBe userdetails.expectedNumberOfLPPPenaltyCards
            userdetails.validatePenaltyCardsContent(lppCards)
          }

          s"the user is an authorised agent for a client with nino $nino" in {
            setFeatureDate(Some(date))
            stubAuthRequests(true, userdetails.nino)
            stubGetPenalties(userdetails.nino, Some("123456789"))(OK, userdetails.getApiResponseJson(userdetails.nino))
            val result = get("/agent", true)
            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyOverview(document, userdetails.expectedOverviewText(true), true)
            validatePenaltyTabs(document)
            if (userdetails.numberOfLSPPenalties == 0) {
              validateNoLSPPenalties(document, true)
            }
            val lppTab = getLPPTabContent(document)
            lppTab.getElementById("lppHeading").text() shouldBe "Late payment penalties"
            lppTab.getElementsByClass("govuk-body").first().text() shouldBe "The earlier your client pays their Income Tax, the lower their penalties and interest will be."
            val lppCards: Elements = lppTab.getElementsByClass("govuk-summary-card")
            lppCards.size() shouldBe userdetails.expectedNumberOfLPPPenaltyCards
            userdetails.validatePenaltyCardsContent(lppCards)
          }
        }
      }
    }
    setFeatureDate(Some(defaultTimeMachineDate))
  }
}
