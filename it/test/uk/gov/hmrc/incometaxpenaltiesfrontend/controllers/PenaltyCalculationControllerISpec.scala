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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{ComponentSpecHelper, NavBarTesterHelper, ViewSpecHelper}

class PenaltyCalculationControllerISpec extends ComponentSpecHelper with ViewSpecHelper with AuthStub
with NavBarTesterHelper {

  "GET /calculation" when {

    testNavBar("/calculation/1")()

    "return an OK with a view" when {
      "have the correct page has correct elements" in {
        stubAuth(OK, successfulIndividualAuthResponse)
        val result = get("/calculation/1")
        result.status shouldBe OK

        val document = Jsoup.parse(result.body)

        document.getServiceName.text() shouldBe "Manage your Self Assessment"
        document.title() shouldBe "First penalty for late payment - Manage your Self Assessment - GOV.UK"
        document.getH1Elements.text() shouldBe "First penalty for late payment"
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
  }
}
