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
import controllers.agent.SessionKeys
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.JsoupUtils.*
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase, PenaltiesWiremockStubs}

import java.time.LocalDate

class PenaltiesControllerISpec extends IntegrationSpecCommonBase with AuthWiremockStubs with PenaltiesWiremockStubs {

  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/")).withSession(
    authToken -> "12345",
    SessionKeys.clientMTDID -> "987654321",
    SessionKeys.clientNino -> "AB123456A"
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

      val response = route(app, fakeAgentRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody.*

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
      select("#lsp-tab p")(3).text shouldBe "If your client reaches 4 points, they’ll have to pay a £200 penalty."
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

    "return page with 2 penalty points when agent's client has 2 late submission penalty (LSP) points" in {
      mockDelegatedResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 2,
            regimeThreshold = 4,
            inactivePenaltyPoints = 0,
            penaltyChargeAmount = 0,
            PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
          ),
          details = Seq(
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("02"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 12, 1),
              penaltyCreationDate = LocalDate.of(2021, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2027-07-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-10-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2027-11-05")),
                returnReceiptDate = None
              )))
            ), sampleLSP.copy(
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

      val response = route(app, fakeAgentRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody.*

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"

      select("#overview h2").text shouldBe "Overview"
      select("#overview p").text shouldBe "Your client's account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "2 late submission penalty points"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"

      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p")(0).select("strong").text shouldBe "2"
      select("#lsp-tab p")(1).text shouldBe "Your client has 2 penalty points for sending late submissions. They should send any missing submissions as soon as possible if they haven't already."
      select("#lsp-tab p")(2).text shouldBe "They'll get another point if they send another submission after a deadline has passed. Points usually expire after 24 months, but it can be longer if they keep sending late submissions."
      select("#lsp-tab p")(3).text shouldBe "If your client reaches 4 points, they’ll have to pay a £200 penalty."
      select("#lsp-tab p a").text shouldBe "Read the guidance about late submission penalties (opens in new tab)"

      { val card1 = select(".app-summary-card").get(0)
        card1.select("header div strong").text shouldBe "ACTIVE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 July 2027 to 5 October 2027"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "5 November 2027"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        rows(4).select("dt").text shouldBe "Point due to expire"
        rows(4).select("dd").text shouldBe "December 2029"

        card1.select("footer div a").text shouldBe "Appeal penalty point 2"
      }

      { val card2 = select(".app-summary-card")(1)

        card2.select("header div strong").text shouldBe "ACTIVE"
        val rows = card2.select(".govuk-summary-list__row")

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

        card2.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 1"
      }
    }

    "return page with 3 penalty points when agent's client has 3 late submission penalty (LSP) points" in {
      mockDelegatedResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 3,
            regimeThreshold = 4,
            inactivePenaltyPoints = 0,
            penaltyChargeAmount = 0,
            PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
          ),
          details = Seq(
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("02"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 12, 1),
              penaltyCreationDate = LocalDate.of(2027, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2027-07-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-10-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2027-11-05")),
                returnReceiptDate = None
              )))
            ),
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("03"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2030, 12, 1),
              penaltyCreationDate = LocalDate.of(2028, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2026-04-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-04-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2028-01-31")),
                returnReceiptDate = None
              )))
            ), sampleLSP.copy(
              penaltyNumber = "1234567890",
              penaltyOrder = Some("01"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 9, 1),
              penaltyCreationDate = LocalDate.of(2027, 11, 1)
            )
          )
        )
        ),
        latePaymentPenalty = None,
        breathingSpace = None
      )

      mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayloadWithAddedPoint))

      val response = route(app, fakeAgentRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody.*

      parsedBody.title shouldBe "Self Assessment penalties and appeals"

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"

      select("#overview h2").text shouldBe "Overview"
      select("#overview p").text shouldBe "Your client's account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "3 late submission penalty points"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"

      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p")(0).select("strong").text shouldBe "3"
      parsedBody.getElementById("warning-text").text shouldBe "! Warning Your client will get a £200 penalty if they send another late submission."
      select("#lsp-tab p")(1).text shouldBe "Your client has 3 penalty points for sending late submissions. They should send any missing submissions as soon as possible if they haven't already."
      select("#lsp-tab p")(2).text shouldBe "They'll get another point if they send another submission after a deadline has passed. Points usually expire after 24 months, but it can be longer if they keep sending late submissions."
      select("#lsp-tab p")(3).text shouldBe "If your client reaches 4 points, they’ll have to pay a £200 penalty."
      select("#lsp-tab p a").text shouldBe "Read the guidance about late submission penalties (opens in new tab)"

      {
        val card1 = select(".app-summary-card")(0)

        card1.select("header div strong").text shouldBe "ACTIVE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Tax year"
        rows(0).select("dd").text shouldBe "2026 to 2027"

        rows(1).select("dt").text shouldBe "Return due"
        rows(1).select("dd").text shouldBe "31 January 2028"

        rows(2).select("dt").text shouldBe "Return submitted"
        rows(2).select("dd").text shouldBe "Not yet received"

        rows(3).select("dt").text shouldBe "Point due to expire"
        rows(3).select("dd").text shouldBe "December 2030"

        card1.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 3"
      }

      {
        val card2 = select(".app-summary-card").get(1)

        card2.select("header div strong").text shouldBe "ACTIVE"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 July 2027 to 5 October 2027"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "5 November 2027"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        rows(4).select("dt").text shouldBe "Point due to expire"
        rows(4).select("dd").text shouldBe "December 2029"

        card2.select("footer div a").text shouldBe "Appeal penalty point 2"
      }

      {
        val card3 = select(".app-summary-card")(2)

        card3.select("header div strong").text shouldBe "ACTIVE"
        val rows = card3.select(".govuk-summary-list__row")

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

        card3.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 1"
      }
    }

    "return page with 4 penalty points and a £200 penalty when agent's client has 4 late submission penalty (LSP) points" in {
      mockDelegatedResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 4,
            regimeThreshold = 4,
            inactivePenaltyPoints = 0,
            penaltyChargeAmount = 200.00,
            PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
          ),
          details = Seq(
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("02"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 12, 1),
              penaltyCreationDate = LocalDate.of(2027, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2027-07-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-10-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2027-11-05")),
                returnReceiptDate = None
              )))
            ),
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("04"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2030, 6, 10),
              penaltyCreationDate = LocalDate.of(2028, 2, 10),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2027-10-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2028-01-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2028-02-07")),
                returnReceiptDate = None
              )))
            ),
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("03"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2030, 12, 1),
              penaltyCreationDate = LocalDate.of(2028, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2026-04-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-04-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2028-01-31")),
                returnReceiptDate = None
              )))
            ), sampleLSP.copy(
              penaltyNumber = "1234567890",
              penaltyOrder = Some("01"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 9, 1),
              penaltyCreationDate = LocalDate.of(2027, 11, 1)
            )
          )
        )
        ),
        latePaymentPenalty = None,
        breathingSpace = None
      )

      mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayloadWithAddedPoint))

      val response = route(app, fakeAgentRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody.*

      parsedBody.title shouldBe "Self Assessment penalties and appeals"

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"

      select("#overview h2").text shouldBe "Overview"
      select("#overview p").text shouldBe "Your client's account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "a late submission penalty"
      select("#overview #your-account-has li:nth-child(2)").text shouldBe "the maximum number of late submission penalty points"
      select("#check-amounts").text shouldBe "Check amounts"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"

      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p")(0).select("strong").text shouldBe "4"
      parsedBody.getElementById("warning-text").text shouldBe "! Warning Your client has been given a £200 penalty for reaching the penalty threshold."
      select("#lsp-tab p")(1).text shouldBe "They will get an additional £200 penalty every time they send a late submission in the future, until their points are removed. They should send any missing submissions as soon as possible if they haven't already."
      select("#lsp-tab p a").text shouldBe "Actions your client must take to get their points removed by December 2028 (opens in new tab)"

      {
        val card1 = select(".app-summary-card")(0)

        card1.select("header div strong").text shouldBe "ACTIVE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 October 2027 to 5 January 2028"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "7 February 2028"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        card1.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 4"
      }

      {
        val card2 = select(".app-summary-card")(1)

        card2.select("header div strong").text shouldBe "ACTIVE"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Tax year"
        rows(0).select("dd").text shouldBe "2026 to 2027"

        rows(1).select("dt").text shouldBe "Return due"
        rows(1).select("dd").text shouldBe "31 January 2028"

        rows(2).select("dt").text shouldBe "Return submitted"
        rows(2).select("dd").text shouldBe "Not yet received"

        card2.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 3"
      }

      {
        val card3 = select(".app-summary-card").get(2)

        card3.select("header div strong").text shouldBe "ACTIVE"
        val rows = card3.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 July 2027 to 5 October 2027"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "5 November 2027"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        card3.select("footer div a").text shouldBe "Appeal penalty point 2"
      }

      {
        val card4 = select(".app-summary-card")(3)

        card4.select("header div strong").text shouldBe "ACTIVE"
        val rows = card4.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 April 2027 to 5 July 2027"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "5 August 2027"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "10 August 2027"

        card4.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 1"
      }
    }

    "return page with 4 penalty points and an additional £200 penalty when agent's client has 5 late submission penalty (LSP) points" in {
      mockDelegatedResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 5,
            regimeThreshold = 4,
            inactivePenaltyPoints = 0,
            penaltyChargeAmount = 200.00,
            PoCAchievementDate = Some(LocalDate.of(2022, 1, 1))
          ),
          details = Seq(
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("02"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 12, 1),
              penaltyCreationDate = LocalDate.of(2027, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2027-07-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-10-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2027-11-05")),
                returnReceiptDate = None
              )))
            ),
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("04"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2030, 6, 10),
              penaltyCreationDate = LocalDate.of(2028, 2, 10),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2027-10-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2028-01-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2028-02-07")),
                returnReceiptDate = None
              )))
            ),
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("03"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2030, 12, 1),
              penaltyCreationDate = LocalDate.of(2028, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2026-04-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2027-04-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2028-01-31")),
                returnReceiptDate = None
              )))
            ),
            sampleLSP.copy(
              penaltyNumber = "1234567891",
              penaltyOrder = Some("05"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2030, 9, 22),
              penaltyCreationDate = LocalDate.of(2028, 4, 1),
              lateSubmissions = Some(Seq(sampleLateSubmission.copy(
                taxPeriodStartDate = Some(LocalDate.parse("2028-01-06")),
                taxPeriodEndDate = Some(LocalDate.parse("2028-04-05")),
                taxPeriodDueDate = Some(LocalDate.parse("2028-04-07")),
                returnReceiptDate = None
              )))
            ), sampleLSP.copy(
              penaltyNumber = "1234567890",
              penaltyOrder = Some("01"),
              FAPIndicator = Some("X"),
              penaltyExpiryDate = LocalDate.of(2029, 9, 1),
              penaltyCreationDate = LocalDate.of(2027, 11, 1)
            )
          )
        )
        ),
        latePaymentPenalty = None,
        breathingSpace = None
      )

      mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayloadWithAddedPoint))

      val response = route(app, fakeAgentRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody.*

      parsedBody.title shouldBe "Self Assessment penalties and appeals"

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"

      select("#overview h2").text shouldBe "Overview"
      select("#overview p").text shouldBe "Your client's account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "late submission penalties"
      select("#overview #your-account-has li:nth-child(2)").text shouldBe "the maximum number of late submission penalty points"
      select("#check-amounts").text shouldBe "Check amounts"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"

      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p")(0).select("strong").text shouldBe "4"
      parsedBody.getElementById("warning-text").text shouldBe "! Warning Your client has been given an additional £200 penalty."
      select("#lsp-tab p")(1).text shouldBe "They will get another £200 penalty every time they send a late submission in the future, until their points are removed. They should send any missing submissions as soon as possible if they haven't already."
      select("#lsp-tab p a").text shouldBe "Actions your client must take to get their points removed by December 2028 (opens in new tab)"

      {
        val card1 = select(".app-summary-card")(0)

        card1.select("header div strong").text shouldBe "ACTIVE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 January 2028 to 5 April 2028"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "7 April 2028"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        card1.select(".app-summary-card footer div a").text shouldBe "Appeal penalty"
      }

      {
        val card2 = select(".app-summary-card")(1)

        card2.select("header div strong").text shouldBe "ACTIVE"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 October 2027 to 5 January 2028"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "7 February 2028"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        card2.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 4"
      }

      {
        val card3 = select(".app-summary-card")(2)

        card3.select("header div strong").text shouldBe "ACTIVE"
        val rows = card3.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Tax year"
        rows(0).select("dd").text shouldBe "2026 to 2027"

        rows(1).select("dt").text shouldBe "Return due"
        rows(1).select("dd").text shouldBe "31 January 2028"

        rows(2).select("dt").text shouldBe "Return submitted"
        rows(2).select("dd").text shouldBe "Not yet received"

        card3.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 3"
      }

      {
        val card4 = select(".app-summary-card").get(3)

        card4.select("header div strong").text shouldBe "ACTIVE"
        val rows = card4.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 July 2027 to 5 October 2027"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "5 November 2027"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "Not yet received"

        card4.select("footer div a").text shouldBe "Appeal penalty point 2"
      }

      {
        val card5 = select(".app-summary-card")(4)

        card5.select("header div strong").text shouldBe "ACTIVE"
        val rows = card5.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Income source"
        rows(0).select("dd").text shouldBe "JB Painting and Decorating"

        rows(1).select("dt").text shouldBe "Quarter"
        rows(1).select("dd").text shouldBe "6 April 2027 to 5 July 2027"

        rows(2).select("dt").text shouldBe "Update due"
        rows(2).select("dd").text shouldBe "5 August 2027"

        rows(3).select("dt").text shouldBe "Update submitted"
        rows(3).select("dd").text shouldBe "10 August 2027"

        card5.select(".app-summary-card footer div a").text shouldBe "Appeal penalty point 1"
      }
    }
  }
  }
