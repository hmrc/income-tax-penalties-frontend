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
import play.api.http.Status.OK
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.AuthStub
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

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
        document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
        document.getH2Elements.get(3).text() shouldBe "Late submission penalties"
        document.getH2Elements.get(6).text() shouldBe "Late payment penalties"
        document.getSubmitButton.text() shouldBe "Check amounts and pay"
      }

      "the user is an authorised agent" in {
        stubAuth(OK, successfulAgentAuthResponse)
        val result = get("/", isAgent = true)

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
        document.getH2Elements.get(0).text() shouldBe "Overview"
        document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
        document.getH2Elements.get(3).text() shouldBe "Late submission penalties"
        document.getH2Elements.get(6).text() shouldBe "Late payment penalties"
        document.getSubmitButton.text() shouldBe "Check amounts and pay"
      }
    }
  }

}
