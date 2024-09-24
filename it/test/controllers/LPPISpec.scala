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

import connectors.PenaltiesConnector.{GetPenaltyDetails, LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty, TimeToPay, lateSubmissionPenaltyFmt}
import play.api.mvc.AnyContentAsEmpty
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import org.jsoup.Jsoup
import utils.JsoupUtils._
import java.time.LocalDate
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase, PenaltiesWiremockStubs}

class LPPISpec extends IntegrationSpecCommonBase with AuthWiremockStubs with PenaltiesWiremockStubs {

  val fakeClientRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/")).withSession(
    authToken -> "12345"
  )

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/"))

  val sampleLPP: LPPDetails = LPPDetails(
    penaltyChargeReference = None,
    principalChargeReference = "XJ002616061027",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyChargeCreationDate = None,
    penaltyStatus = LPPPenaltyStatusEnum.Accruing,
    penaltyAmountPaid = None,
    penaltyAmountPosted = 0,
    penaltyAmountAccruing = 400.00,
    penaltyAmountOutstanding = None,
    LPP1LRDays = Some("15"),
    LPP1HRDays = Some("30"),
    LPP2Days = None,
    LPP1LRCalculationAmount = Some(20000.00),
    LPP1HRCalculationAmount = None,
    LPP1LRPercentage = Some(2),
    LPP1HRPercentage = Some(2),
    LPP2Percentage = None,
    communicationsDate = None,
    penaltyChargeDueDate = Some(LocalDate.parse("2028-01-31")),
    appealInformation = None,
    principalChargeBillingFrom = LocalDate.parse("2026-04-06"),
    principalChargeBillingTo = LocalDate.parse("2027-04-05"),
    principalChargeDueDate = LocalDate.parse("2028-01-31"),
    principalChargeLatestClearing = None,
    principalChargeDocNumber = "DOC1",
    principalChargeMainTransaction = "4720",
    principalChargeSubTransaction = "SUB1",
    timeToPay = None
  )

  "GET /" should {
    "redirect to the login page when the user is not logged in" in {
      mockUnauthorisedResponse()
      val response = route(app, fakeAnonymousRequest).get
      redirectLocation(response) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9185%2Fincome-tax-penalties-frontend")
    }

    "return page with 1 estimated penalty point when user has 1 late payment penalty (LPP) point in total" in {
      mockEnroledResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty =None,
        latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP), true)),
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
      select("#overview p").text shouldBe "Your account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "overdue Income Tax charges"
      select("#overview #your-account-has li:nth-child(2)").text shouldBe "unpaid interest"
      select("#overview #your-account-has li:nth-child(3)").text shouldBe "a late payment penalty"
      select("#check-amounts").text shouldBe "Check amounts and pay"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a")(0).text shouldBe "Late submission penalties"
      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p").text shouldBe "You don't have any late submission penalties."
      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item > a")(1).text shouldBe "Late payment penalties"

      select("#lpp-tab h3").text shouldBe "Late payment penalties"
      select("#lpp-tab p")(0).text shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
      select("#lpp-tab p a").text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

      //select(".app-summary-card").dump("card")
      select(".app-summary-card header div strong").text shouldBe "ESTIMATE"
      val rows = select(".govuk-summary-list__row")

      rows(0).select("dt").text shouldBe "Penalty type"
      rows(0).select("dd").text shouldBe "First penalty for late payment"

      rows(1).select("dt").text shouldBe "Overdue charge"
      rows(1).select("dd").text shouldBe "Income Tax for 2026 to 2027 tax year"

      rows(2).select("dt").text shouldBe "Income Tax due"
      rows(2).select("dd").text shouldBe "31 January 2028"

      rows(3).select("dt").text shouldBe "Income Tax paid"
      rows(3).select("dd").text shouldBe "Payment not yet received"

      select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
    }

    "return page with 1 estimated penalty point and 1 paid penalty point when user has 2 late payment penalty (LPP) points in total" in {
      mockEnroledResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(LatePaymentPenalty(
          details = Seq(
            sampleLPP.copy(
            principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
            principalChargeBillingTo = LocalDate.parse("2028-04-05"),
            principalChargeDueDate = LocalDate.parse("2029-01-31")
        ), sampleLPP.copy(
            principalChargeReference = "XJ002616061028",
            penaltyStatus = LPPPenaltyStatusEnum.Posted,
            penaltyAmountAccruing = 0,
            penaltyAmountPosted = 400.00,
            penaltyAmountPaid = Some(400.00),
            penaltyAmountOutstanding = Some(0),
            principalChargeLatestClearing = Some(LocalDate.parse("2028-02-19"))
          )
          ),manualLPPIndicator = false
        ) 
          ),
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
      select("#overview p").text shouldBe "Your account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "overdue Income Tax charges"
      select("#overview #your-account-has li:nth-child(2)").text shouldBe "unpaid interest"
      select("#overview #your-account-has li:nth-child(3)").text shouldBe "a late payment penalty"
      select("#check-amounts").text shouldBe "Check amounts and pay"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a")(0).text shouldBe "Late submission penalties"
      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p").text shouldBe "You don't have any late submission penalties."
      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item > a")(1).text shouldBe "Late payment penalties"

      select("#lpp-tab h3").text shouldBe "Late payment penalties"
      select("#lpp-tab p")(0).text shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
      select("#lpp-tab p a").text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

      {
        val card1 = select(".app-summary-card").get(0)
        card1.select(".app-summary-card header div strong").text shouldBe "ESTIMATE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "Payment not yet received"

        card1.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
      }

      {
        val card2 = select(".app-summary-card").get(1)
        card2.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2026 to 2027 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2028"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "19 February 2028"

        card2.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card2.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }
    }

    "return page with 1 estimated penalty point, 1 due penalty point and 1 paid penalty point when user has 2 late payment penalty (LPP) points in total" in {
      mockEnroledResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(LatePaymentPenalty(
          details = Seq(
            sampleLPP.copy(
              penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
              penaltyAmountAccruing = 2.19,
              LPP2Percentage = Some(4.00),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31")
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061028",
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 800.00,
              penaltyAmountPaid = Some(0),
              penaltyAmountOutstanding = Some(800.00),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31")
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061029",
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 400.00,
              penaltyAmountPaid = Some(400.00),
              penaltyAmountOutstanding = Some(0),
              principalChargeLatestClearing = Some(LocalDate.parse("2028-02-19"))
            )
          ), manualLPPIndicator = false
        )
        ),
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
      select("#overview p").text shouldBe "Your account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "overdue Income Tax charges"
      select("#overview #your-account-has li:nth-child(2)").text shouldBe "unpaid interest"
      select("#overview #your-account-has li:nth-child(3)").text shouldBe "late payment penalties"
      select("#check-amounts").text shouldBe "Check amounts and pay"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a")(0).text shouldBe "Late submission penalties"
      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p").text shouldBe "You don't have any late submission penalties."
      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item > a")(1).text shouldBe "Late payment penalties"

      select("#lpp-tab h3").text shouldBe "Late payment penalties"
      select("#lpp-tab p")(0).text shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
      select("#lpp-tab p a").text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

      {
        val card1 = select(".app-summary-card").get(0)
        card1.select(".app-summary-card header div strong").text shouldBe "ESTIMATE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "Second penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "Payment not yet received"

        card1.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
      }

      {
        val card2 = select(".app-summary-card").get(1)
        card2.select(".app-summary-card header div strong").text shouldBe "DUE"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "Payment not yet received"

        card2.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card2.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }

      {
        val card3 = select(".app-summary-card").get(2)
        card3.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card3.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2026 to 2027 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2028"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "19 February 2028"

        card3.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card3.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }
    }

    "return page with 1 estimated penalty point with increase, 1 due penalty point and 1 paid penalty point when user has 2 late payment penalty (LPP) points in total" in {
      mockEnroledResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(LatePaymentPenalty(
          details = Seq(
            sampleLPP.copy(
              penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
              penaltyAmountAccruing = 46.02,
              penaltyAmountPaid = Some(0),
              penaltyAmountOutstanding = Some(0),
              LPP2Percentage = Some(4.00),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31")
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061028",
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 800.00,
              penaltyAmountPaid = Some(0),
              penaltyAmountOutstanding = Some(800.00),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31")
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061029",
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 400.00,
              penaltyAmountPaid = Some(400.00),
              penaltyAmountOutstanding = Some(0),
              principalChargeLatestClearing = Some(LocalDate.parse("2028-02-19"))
            )
          ), manualLPPIndicator = false
        )
        ),
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
      select("#overview p").text shouldBe "Your account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "overdue Income Tax charges"
      select("#overview #your-account-has li:nth-child(2)").text shouldBe "unpaid interest"
      select("#overview #your-account-has li:nth-child(3)").text shouldBe "late payment penalties"
      select("#check-amounts").text shouldBe "Check amounts and pay"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a")(0).text shouldBe "Late submission penalties"
      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p").text shouldBe "You don't have any late submission penalties."
      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item > a")(1).text shouldBe "Late payment penalties"
      select("#lpp-tab h3").text shouldBe "Late payment penalties"
      select("#lpp-tab p")(0).text shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
      select("#lpp-tab p a").text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

      {
        val card1 = select(".app-summary-card").get(0)
        card1.select(".app-summary-card header div strong").text shouldBe "ESTIMATE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "Second penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "Payment not yet received"

        card1.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
      }

      {
        val card2 = select(".app-summary-card").get(1)
        card2.select(".app-summary-card header div strong").text shouldBe "DUE"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "Payment not yet received"

        card2.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card2.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }

      {
        val card3 = select(".app-summary-card").get(2)
        card3.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card3.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2026 to 2027 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2028"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "19 February 2028"

        card3.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card3.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }
    }

    "return page with 1 due penalty point and 2 paid penalty points when user has 2 late payment penalty (LPP) points in total" in {
      mockEnroledResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(LatePaymentPenalty(
          details = Seq(
            sampleLPP.copy(
              penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
              penaltyAmountAccruing = 0,
              penaltyAmountPaid = Some(0),
              penaltyAmountPosted = 46.02,
              penaltyAmountOutstanding = Some(46.02),
              LPP2Percentage = Some(4.00),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31"),
              principalChargeLatestClearing = Some(LocalDate.parse("2029-03-23"))
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061028",
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 800.00,
              penaltyAmountPaid = Some(800.00),
              penaltyAmountOutstanding = Some(0),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31"),
              principalChargeLatestClearing = Some(LocalDate.parse("2029-03-23"))
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061029",
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 400.00,
              penaltyAmountPaid = Some(400.00),
              penaltyAmountOutstanding = Some(0),
              principalChargeLatestClearing = Some(LocalDate.parse("2028-02-19"))
            )
          ), manualLPPIndicator = false
        )
        ),
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
      select("#overview p").text shouldBe "Your account has:"
      select("#overview #your-account-has li:nth-child(1)").text shouldBe "1 late payment penalty"
      select("#check-amounts").text shouldBe "Check amounts and pay"
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a")(0).text shouldBe "Late submission penalties"
      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p").text shouldBe "You don't have any late submission penalties."
      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item > a")(1).text shouldBe "Late payment penalties"
      select("#lpp-tab h3").text shouldBe "Late payment penalties"
      select("#lpp-tab p")(0).text shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
      select("#lpp-tab p a").text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

      {
        val card1 = select(".app-summary-card").get(0)
        card1.select(".app-summary-card header div strong").text shouldBe "DUE"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "Second penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "23 March 2029"

        card1.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card1.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }

      {
        val card2 = select(".app-summary-card").get(1)
        card2.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card2.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "23 March 2029"

        card2.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card2.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }

      {
        val card3 = select(".app-summary-card").get(2)
        card3.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card3.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2026 to 2027 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2028"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "19 February 2028"

        card3.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card3.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }
    }

    "return page with all late payment penalty (LPP) points displayed as paid" in {
      mockEnroledResponse()

      val getPenaltyDetailsPayloadWithAddedPoint = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(LatePaymentPenalty(
          details = Seq(
            sampleLPP.copy(
              penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPaid = Some(46.02),
              penaltyAmountPosted = 46.02,
              penaltyAmountOutstanding = Some(0),
              LPP2Percentage = Some(4.00),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31"),
              principalChargeLatestClearing = Some(LocalDate.parse("2029-03-23"))
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061028",
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 800.00,
              penaltyAmountPaid = Some(800.00),
              penaltyAmountOutstanding = Some(0),
              principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
              principalChargeBillingTo = LocalDate.parse("2028-04-05"),
              principalChargeDueDate = LocalDate.parse("2029-01-31"),
              principalChargeLatestClearing = Some(LocalDate.parse("2029-03-23"))
            ), sampleLPP.copy(
              principalChargeReference = "XJ002616061029",
              penaltyStatus = LPPPenaltyStatusEnum.Posted,
              penaltyAmountAccruing = 0,
              penaltyAmountPosted = 400.00,
              penaltyAmountPaid = Some(400.00),
              penaltyAmountOutstanding = Some(0),
              principalChargeLatestClearing = Some(LocalDate.parse("2028-02-19"))
            )
          ), manualLPPIndicator = false
        )
        ),
        breathingSpace = None
      )

      mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayloadWithAddedPoint))

      val response = route(app, fakeClientRequest).get
      status(response) shouldBe Status.OK
      val parsedBody = Jsoup.parse(contentAsString(response))
      import parsedBody._

      parsedBody.title shouldBe "Self Assessment penalties and appeals"

      select("#main-content h1").text shouldBe "Self Assessment penalties and appeals"
      
      select("#penalty-and-appeal-details h2").text shouldBe "Penalty and appeal details"

      select("#penalty-and-appeal-details > ul > li.govuk-tabs__list-item.govuk-tabs__list-item--selected > a").text shouldBe "Late submission penalties"
      select("#lsp-tab h3").text shouldBe "Late submission penalties"
      select("#lsp-tab p").text shouldBe "You don't have any late submission penalties."
      select("#lpp-tab h3").text shouldBe "Late payment penalties"
      select("#lpp-tab p")(0).text shouldBe "The earlier you pay your Income Tax, the lower your penalties and interest will be."
      select("#lpp-tab p a").text shouldBe "Read the guidance about how late payment penalties are calculated (opens in a new tab)"

      {
        val card1 = select(".app-summary-card").get(0)
        card1.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card1.select(".govuk-summary-list__row")

        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "Second penalty for late payment"

        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"

        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"

        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "23 March 2029"

        card1.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card1.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }

      {
        val card2 = select(".app-summary-card").get(1)
        card2.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card2.select(".govuk-summary-list__row")
        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"
        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2027 to 2028 tax year"
        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2029"
        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "23 March 2029"
        card2.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card2.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }
      {
        val card3 = select(".app-summary-card").get(2)
        card3.select(".app-summary-card header div strong").text shouldBe "PAID"
        val rows = card3.select(".govuk-summary-list__row")
        rows(0).select("dt").text shouldBe "Penalty type"
        rows(0).select("dd").text shouldBe "First penalty for late payment"
        rows(1).select("dt").text shouldBe "Overdue charge"
        rows(1).select("dd").text shouldBe "Income Tax for 2026 to 2027 tax year"
        rows(2).select("dt").text shouldBe "Income Tax due"
        rows(2).select("dd").text shouldBe "31 January 2028"
        rows(3).select("dt").text shouldBe "Income Tax paid"
        rows(3).select("dd").text shouldBe "19 February 2028"
        card3.select(".app-summary-card footer div a")(0).text shouldBe "View calculation"
        card3.select(".app-summary-card footer div a")(1).text shouldBe "Appeal this penalty"
      }
    }
  }
}

