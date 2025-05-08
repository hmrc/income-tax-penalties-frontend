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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub

class IndexControllerISpec extends ControllerISpecHelper with FeatureSwitching
  with PenaltiesStub with PenaltiesDetailsTestData {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  "GET /penalties/income-tax" should {
    "when call to penalties backend returns data" when {
      "the user is an authorised individual" should {
        "have the correct page has correct elements" in {
          stubAuthRequests(false)
          stubGetPenalties("1234567890", None)(OK, Json.toJson(samplePenaltyDetailsModel))

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
          document.getSubmitButton.text() shouldBe "Check amounts and pay"
        }
      }

      "the user is an authorised agent" should {
        "have the correct page has correct elements" in {
          stubAuthRequests(true)
          stubGetPenalties("1234567890", Some("123456789"))(OK, Json.toJson(samplePenaltyDetailsModel))

          val result = get("/", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
          document.getH2Elements.get(0).text() shouldBe "Overview"
          document.getParagraphs.get(0).text() shouldBe "Your client’s account has:"
          document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
          document.getH3Elements.get(0).text() shouldBe "Late submission penalties"
          document.getH3Elements.get(1).text() shouldBe "Late payment penalties"
          document.getSubmitButton.text() shouldBe "Check amounts"
        }
      }
    }

    "when call to penalties backend fails" should {
      "render an ISE" in {
        stubAuthRequests(false)
        stubGetPenalties("1234567890", None)(INTERNAL_SERVER_ERROR, Json.obj())

        val result = get("/")

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body should include("Sorry, there is a problem with the service")
      }
    }
  }
}
