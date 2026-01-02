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

class IndexControllerLSPOnlyISpec extends LSPControllerHelper with FeatureSwitching
  with PenaltiesStub with PenaltiesDetailsTestData {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }
  
  "GET /view-penalty/self-assessment" when {
    "the call to penalties backend returns data" should {
      "render the expected penalty cards" when {
        lspUsers.foreach { case (nino, userdetails) =>
          s"the user with nino $nino is an authorised individual" in {
            stubAuthRequests(false, nino)
            stubGetPenalties(nino, None)(OK, userdetails.getApiResponseJson(nino))
            val result = get("/")
            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validatePenaltyTabs(document)
            validateNoLPPPenalties(document)
            val lspTab = getLSPTabContent(document)
            lspTab.getElementById("lspHeading").text() shouldBe "Late submission penalties"
            lspTab.getElementsByClass("govuk-body").first().text() shouldBe expectedLSPTabBody(userdetails)
            val lspCards: Elements = lspTab.getElementsByClass("govuk-summary-card")
            lspCards.size() shouldBe userdetails.expectedNumberOfLSPPenaltyCards
            userdetails.validatePenaltyCardsContent(lspCards)
          }

          s"the user is an authorised agent for a client with nino $nino" in {
            stubAuthRequests(true, nino)
            stubGetPenalties(nino, Some("123456789"))(OK, userdetails.getApiResponseJson(nino))
            val result = get("/agent", true)
            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
            validateNoLPPPenalties(document, true)
            validatePenaltyTabs(document)
            validateNoLPPPenalties(document, true)
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
