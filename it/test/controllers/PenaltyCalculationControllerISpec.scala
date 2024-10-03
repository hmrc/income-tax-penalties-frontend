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
import org.jsoup.Jsoup
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import java.time.LocalDate
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase, PenaltiesWiremockStubs}
import play.api.mvc.AnyContentAsEmpty
import uk.gov.hmrc.http.SessionKeys.authToken

class PenaltyCalculationControllerISpec extends IntegrationSpecCommonBase with AuthWiremockStubs with PenaltiesWiremockStubs {
  
  val fakeClientRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/calculation/XJ002616061027")).withSession(
    authToken -> "12345"
  )

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/calculation/XJ002616061027"))

  val sampleLPP: LPPDetails = LPPDetails(
    penaltyChargeReference = Some("XJ002616061027"),
    principalChargeReference = "XJ002616061027",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyChargeCreationDate = None,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(400.00),
    penaltyAmountPosted = 400.00,
    penaltyAmountAccruing = 0.00,
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
    principalChargeLatestClearing = Some(LocalDate.parse("2028-02-19")),
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
  }

  "return page with calculation (lower rate) when penalty is paid" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyAmountOutstanding = Some(0)
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2026 to 2027"
    select("#main-content h1").text shouldBe "First penalty for late payment"

    getElementById("paragraph1").text shouldBe "This penalty applies if Income Tax has not been paid for 15 days."
    getElementById("paragraph2").text shouldBe "The calculation we use is: 2% of £20000.00 (the unpaid Income Tax 15 days after the due date)"

    getElementById("key1").text shouldBe "Penalty amount"
    getElementById("value1").text shouldBe "£400.00"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£400.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£0.00"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (lower rate) when penalty is an estimate" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(0),
        penaltyAmountPosted = 0,
        penaltyAmountAccruing = 400.00,
        principalChargeLatestClearing = None,
        penaltyChargeDueDate = None
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2026 to 2027"
    select("#main-content h1").text shouldBe "First penalty for late payment"

    getElementById("paragraph1").text shouldBe "This penalty applies if Income Tax has not been paid for 15 days."
    getElementById("paragraph2").text shouldBe "The calculation we use is: 2% of £20000.00 (the unpaid Income Tax 15 days after the due date)"

    getElementById("key1").text shouldBe "Penalty amount (estimate)"
    getElementById("value1").text shouldBe "£400.00"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£0.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£400.00"

    getElementsByClass("govuk-warning-text").text shouldBe "! The penalty will increase by a further 2% of the unpaid Income Tax, if Income Tax remains unpaid 30 days after the due date."

    getElementsByClass("govuk-heading-s").text shouldBe "Estimates"
    getElementById("estimates").text shouldBe "Penalties will show as estimates until:"

    select("#main-content li:nth-child(1)").text shouldBe "you pay the Income Tax bill, or"
    select("#main-content li:nth-child(2)").text shouldBe "30 days have passed since the Income Tax due date"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (higher rate) when penalty is due" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyAmountPaid = Some(0),
        penaltyAmountPosted = 800.00,
        principalChargeLatestClearing = None,
        penaltyChargeDueDate = None,
        penaltyAmountOutstanding = Some(800.00),
        LPP1HRCalculationAmount = Some(20000.00),
        principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
        principalChargeBillingTo = LocalDate.parse("2028-04-05"),
        principalChargeDueDate = LocalDate.parse("2029-01-31"),
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2027 to 2028"
    select("#main-content h1").text shouldBe "First penalty for late payment"

    getElementById("paragraph1").text shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
    getElementById("paragraph2").text shouldBe "The calculation we use is: 2% of £20000.00 (the unpaid Income Tax 15 days after the due date) 2% of £20000.00 (the unpaid Income Tax 30 days after the due date)"

    getElementById("key4").text shouldBe "Due date"
    getElementById("value4").text shouldBe "31 January 2029 OVERDUE"

    getElementById("key1").text shouldBe "Penalty amount"
    getElementById("value1").text shouldBe "£800.00"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£0.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£800.00"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (higher rate) when penalty is an estimate" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountAccruing = 400.00,
        penaltyAmountPaid = Some(0),
        penaltyAmountPosted = 0,
        principalChargeLatestClearing = None,
        penaltyChargeDueDate = None,
        penaltyAmountOutstanding = Some(800.00),
        LPP1HRCalculationAmount = Some(20000.00),
        principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
        principalChargeBillingTo = LocalDate.parse("2028-04-05"),
        principalChargeDueDate = LocalDate.parse("2029-01-31"),
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2027 to 2028"
    select("#main-content h1").text shouldBe "First penalty for late payment"

    getElementById("paragraph1").text shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
    getElementById("paragraph2").text shouldBe "The calculation we use is: 2% of £20000.00 (the unpaid Income Tax 15 days after the due date) 2% of £20000.00 (the unpaid Income Tax 30 days after the due date)"

    getElementById("key1").text shouldBe "Penalty amount (estimate)"
    getElementById("value1").text shouldBe "£400.00"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£0.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£400.00"

    getElementsByClass("govuk-warning-text").text shouldBe "! The penalty will increase by a further 2% of the unpaid Income Tax, if Income Tax remains unpaid 30 days after the due date."

    getElementsByClass("govuk-heading-s").text shouldBe "Estimates"
    getElementById("estimates").text shouldBe "Penalties will show as estimates until:"

    select("#main-content li:nth-child(1)").text shouldBe "you pay the Income Tax bill, or"
    select("#main-content li:nth-child(2)").text shouldBe "30 days have passed since the Income Tax due date"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (higher rate) when penalty is paid" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyAmountPaid = Some(800.00),
        penaltyAmountPosted = 800.00,
        principalChargeLatestClearing = None,
        penaltyChargeDueDate = None,
        penaltyAmountOutstanding = Some(0.00),
        LPP1HRCalculationAmount = Some(20000.00)
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2026 to 2027"
    select("#main-content h1").text shouldBe "First penalty for late payment"

    getElementById("paragraph1").text shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
    getElementById("paragraph2").text shouldBe "The calculation we use is: 2% of £20000.00 (the unpaid Income Tax 15 days after the due date) 2% of £20000.00 (the unpaid Income Tax 30 days after the due date)"

    getElementById("key1").text shouldBe "Penalty amount"
    getElementById("value1").text shouldBe "£800.00"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£800.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£0.00"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (daily rate) when penalty is paid" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
        penaltyAmountPosted = 46.02,
        penaltyAmountOutstanding = Some(0),
        penaltyAmountPaid = Some(46.02),
        LPP2Percentage = Some(4),
        principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
        principalChargeBillingTo = LocalDate.parse("2028-04-05"),
        principalChargeDueDate = LocalDate.parse("2029-01-31"),
        principalChargeLatestClearing = Some(LocalDate.parse("2029-03-23")),
        penaltyChargeDueDate = None
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2027 to 2028"
    select("#main-content h1").text shouldBe "Second penalty for late payment"

    getElementById("paragraph1-lpp2").text shouldBe "This penalty applies from day 31, if any Income Tax remains unpaid."
    getElementById("paragraph2-lpp2").text shouldBe "The total builds up daily until you pay your Income Tax or set up a payment plan."
    getElementById("paragraph3-lpp2").text shouldBe "The calculation we use for each day is: (Penalty rate of 4% x unpaid Income Tax) ÷ days in a year"

    getElementById("key1").text shouldBe "Penalty amount"
    getElementById("value1").text shouldBe "£46.02"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£46.02"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£0.00"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (daily rate) when penalty is an estimate" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
        penaltyStatus = LPPPenaltyStatusEnum.Accruing,
        penaltyAmountPaid = Some(0),
        penaltyAmountPosted = 0,
        penaltyAmountAccruing = 46.02,
        principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
        principalChargeBillingTo = LocalDate.parse("2028-04-05"),
        principalChargeDueDate = LocalDate.parse("2029-01-31"),
        principalChargeLatestClearing = None,
        penaltyChargeDueDate = None
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2027 to 2028"
    select("#main-content h1").text shouldBe "Second penalty for late payment"

    getElementById("paragraph1-lpp2").text shouldBe "This penalty applies from day 31, if any Income Tax remains unpaid."
    getElementById("paragraph2-lpp2").text shouldBe "The total builds up daily until you pay your Income Tax or set up a payment plan."
    getElementById("paragraph3-lpp2").text shouldBe "The calculation we use for each day is: (Penalty rate of 4% x unpaid Income Tax) ÷ days in a year"

    getElementById("key1").text shouldBe "Penalty amount (estimate)"
    getElementById("value1").text shouldBe "£46.02"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£0.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£46.02"

    getElementsByClass("govuk-heading-s").text shouldBe "Estimates"
    getElementById("estimates-lpp2").text shouldBe "Penalties and interest will show as estimates until you pay the charge they relate to."

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

  "return page with calculation (daily rate) when penalty is due" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
        penaltyAmountPaid = Some(0),
        penaltyAmountPosted = 46.02,
        principalChargeLatestClearing = None,
        penaltyChargeDueDate = None,
        penaltyAmountOutstanding = Some(46.02),
        LPP1HRCalculationAmount = Some(20000.00),
        principalChargeBillingFrom = LocalDate.parse("2027-04-06"),
        principalChargeBillingTo = LocalDate.parse("2028-04-05"),
        principalChargeDueDate = LocalDate.parse("2029-01-31"),
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeClientRequest).get
    status(response) shouldBe Status.OK
    val parsedBody = Jsoup.parse(contentAsString(response))
    import parsedBody._

    parsedBody.title shouldBe "Manage your Self Assessment - GOV.UK"

    select("#main-content h2").text shouldBe "Income Tax year 2027 to 2028"
    select("#main-content h1").text shouldBe "Second penalty for late payment"

    getElementById("paragraph1-lpp2").text shouldBe "This penalty applies from day 31, if any Income Tax remains unpaid."
    getElementById("paragraph2-lpp2").text shouldBe "The total builds up daily until you pay your Income Tax or set up a payment plan."
    getElementById("paragraph3-lpp2").text shouldBe "The calculation we use for each day is: (Penalty rate of 4% x unpaid Income Tax) ÷ days in a year"

    getElementById("key4").text shouldBe "Due date"
    getElementById("value4").text shouldBe "31 January 2029 OVERDUE"

    getElementById("key1").text shouldBe "Penalty amount"
    getElementById("value1").text shouldBe "£46.02"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£0.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£46.02"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

}
