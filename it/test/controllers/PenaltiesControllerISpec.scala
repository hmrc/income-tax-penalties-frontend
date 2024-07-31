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

package controllers

import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.JsoupUtils._
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase}

class PenaltiesControllerISpec extends IntegrationSpecCommonBase with AuthWiremockStubs {

  val fakeClientRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/")).withSession(
    authToken -> "12345"
  )

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/"))

  "GET /" should {
    "redirect to the login page when the user is not logged in" in {
      mockUnauthorisedResponse()
      val response = route(app, fakeAnonymousRequest).get
      redirectLocation(response) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9000%2Fincome-tax-penalties-frontend")
    }

    "return page with 1 penalty point when user has 1 penalty point" in {
      mockEnroledResponse()
      //      getPenaltyDetailsStub()
      //      complianceDataStub()
      val response = route(app, fakeClientRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody._

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"

      select("#overview h2").text shouldBe "Overview"
      select("#overview p").text shouldBe "Your account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "1 late submission penalty point"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"

      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p")(0).text shouldBe "You have 1 penalty point for sending a late update."
      select("#lsp-tab p")(1).text shouldBe "You'll get another point if you send another update after a deadline had passed. Points usually expire after 24 months, but it can be longer if you keep sending late updates."
      select("#lsp-tab p")(2).text shouldBe "If you reach 4 points you’ll have to pay a £200 penalty."
      select("#lsp-tab p a").text shouldBe "Read the guidance about late submission penalties (opens in a new tab)"

      select(".app-summary-card").dump("card")
      select(".app-summary-card header div strong").text shouldBe "ACTIVE"
      val rows = select(".govuk-summary-list__row")

      rows(0).select("dt").text shouldBe "Income source"
      rows(0).select("dd").text shouldBe "JB Painting and Decorating"

      rows(1).select("dt").text shouldBe "Quarter"
      rows(1).select("dd").text shouldBe "6 April 2027 to 5 July 2027"

      rows(2).select("dt").text shouldBe "Update due"
      rows(2).select("dd").text shouldBe "5 August 2027"

      rows(3).select("dt").text shouldBe "Update submitted"
      rows(3).select("dd").text shouldBe "10 August 2027"

      rows(4).select("dt").text shouldBe "Point due to expire"
      rows(4).select("dd").text shouldBe "September 2029"

      select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 1"
    }
  }
}
