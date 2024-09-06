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

package agents

import connectors.PenaltiesConnector.{GetPenaltyDetails, LSPDetails, LSPPenaltyCategoryEnum, LSPPenaltyStatusEnum, LSPSummary, LateSubmission, LateSubmissionPenalty, TaxReturnStatusEnum}
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.JsoupUtils._
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase, PenaltiesWiremockStubs}
import controllers.PenaltiesController

import java.time.LocalDate

class PenaltiesControllerISpec extends IntegrationSpecCommonBase with AuthWiremockStubs with PenaltiesWiremockStubs {

  val fakeClientRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/")).withSession(
    authToken -> "12345"
  )

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/"))

  val sampleLateSubmission: LateSubmission = LateSubmission(
    taxPeriodStartDate = Some(LocalDate.parse("2027-04-06")),
    taxPeriodEndDate = Some(LocalDate.parse("2027-07-05")),
    taxPeriodDueDate = Some(LocalDate.parse("2027-08-05")),
    returnReceiptDate = Some(LocalDate.parse("2027-08-10")),
    taxReturnStatus = Some(TaxReturnStatusEnum.Fulfilled)
  )

  val sampleLSP: LSPDetails = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = Some("01"),
    penaltyCategory = Some(LSPPenaltyCategoryEnum.Point),
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    incomeSourceName = Some("JB Painting and Decorating"),
    FAPIndicator = None,
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2029-10-01"),
    expiryReason = None,
    communicationsDate = Some(LocalDate.parse("2069-10-30")),
    lateSubmissions = Some(Seq(sampleLateSubmission)),
    appealInformation = None,
    chargeAmount = None,
    chargeOutstandingAmount = None,
    chargeDueDate = None
  )

  "GET /" should {
    "redirect to the login page when the agent is not logged in" in {
      mockUnauthorisedResponse()
      val response = route(app, fakeAnonymousRequest).get
      redirectLocation(response) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9185%2Fincome-tax-penalties-frontend")
    }

    "return page with 1 penalty point when agent's client has 1 late submission penalty (LSP) point" in {
      mockDelegatedResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 1,
            regimeThreshold = 4,
            inactivePenaltyPoints = 0,
            penaltyChargeAmount = 0,
            PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
          ),
          details = Seq(
            sampleLSP.copy(
              penaltyNumber = "1234567890",
              penaltyOrder = Some("01"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 9, 1),
              penaltyCreationDate = LocalDate.of(2021, 1, 1)
            )
          )
        )
        ),
        latePaymentPenalty = None,
        breathingSpace = None
      )

      mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayloadWithAddedPoint))

      val response = route(app, fakeClientRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody._

      parsedBody.title shouldBe "Self Assessment penalties and appeals"

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"

      select("#overview h2").text shouldBe "Overview"
      select("#overview p").text shouldBe "Your client's account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "1 late submission penalty point"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"

      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p")(0).select("strong").text shouldBe "1"
      select("#lsp-tab p")(1).text shouldBe "Your client has 1 penalty point for sending a late submission. They should send this missing submission as soon as possible if they haven't already."
      select("#lsp-tab p")(2).text shouldBe "They'll get another point if they send another submission after a deadline has passed. Points usually expire after 24 months, but it can be longer if they keep sending late submissions."
      select("#lsp-tab p")(3).text shouldBe "If they reach 4 points they’ll have to pay a £200 penalty."
      select("#lsp-tab p a").text shouldBe "Read the guidance about late submission penalties (opens in new tab)"

      //select(".app-summary-card").dump("card")
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
