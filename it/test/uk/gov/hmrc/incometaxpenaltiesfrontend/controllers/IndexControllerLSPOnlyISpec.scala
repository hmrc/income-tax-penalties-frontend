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
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.indexPage.lsp.LSPControllerHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub

import java.time.LocalDate

class IndexControllerLSPOnlyISpec extends LSPControllerHelper with FeatureSwitching
  with PenaltiesStub with PenaltiesDetailsTestData {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  val defaultTimeMachineDate: LocalDate = getFeatureDate(appConfig)
  
  "GET /view-penalty/self-assessment" when {
    "the call to penalties backend returns data" should {
      "render the expected penalty cards" when {
        lspUsers.foreach { case (nino, userdetails) => 

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

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyOverview(document, userdetails.expectedOverviewText(false),
              userdetails.numberOfUnpaidFinancialPenalties > 0 || userdetails.expectedNumberOfLPPPenaltyCards > 0)
            validatePenaltyTabs(document)
            if (userdetails.expectedNumberOfLPPPenaltyCards == 0) {
              validateNoLPPPenalties(document)
            }
            val lspTab = getLSPTabContent(document)
            lspTab.getElementById("lspHeading").text() shouldBe "Late submission penalties"
            lspTab.getElementsByClass("govuk-body").first().text() shouldBe expectedLSPTabBody(userdetails)
            val lspCards: Elements = lspTab.getElementsByClass("govuk-summary-card")
            lspCards.size() shouldBe userdetails.expectedNumberOfLSPPenaltyCards
            userdetails.validatePenaltyCardsContent(lspCards)
          }

          s"the user is an authorised agent for a client with nino $nino" in {
            setFeatureDate(Some(date))
            stubAuthRequests(true, userdetails.nino)
            stubGetPenalties(userdetails.nino, Some("123456789"))(OK, userdetails.getApiResponseJson(userdetails.nino))
            val result = get("/agent", true)
            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyOverview(document, userdetails.expectedOverviewText(true),
              userdetails.numberOfUnpaidFinancialPenalties > 0 || userdetails.expectedNumberOfLPPPenaltyCards > 0, true)
            validatePenaltyTabs(document)
            if (userdetails.expectedNumberOfLPPPenaltyCards == 0) {
              validateNoLPPPenalties(document, true)
            }
            val lspTab = getLSPTabContent(document)
            lspTab.getElementById("lspHeading").text() shouldBe "Late submission penalties"
            lspTab.getElementsByClass("govuk-body").first().text() shouldBe expectedLSPTabBody(userdetails, true)
            val lspCards: Elements = lspTab.getElementsByClass("govuk-summary-card")
            lspCards.size() shouldBe userdetails.expectedNumberOfLSPPenaltyCards
            userdetails.validatePenaltyCardsContent(lspCards)
          }
        }
      }
    }
  }
}
