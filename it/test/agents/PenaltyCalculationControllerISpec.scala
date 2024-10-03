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

import connectors.PenaltiesConnector.{GetPenaltyDetails, LPPDetails, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty, TimeToPay, lateSubmissionPenaltyFmt}
import play.api.mvc.AnyContentAsEmpty
import org.jsoup.Jsoup
import play.api.http.Status
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase, PenaltiesWiremockStubs}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys.authToken
import controllers.agent.SessionKeys
import play.api.test.Helpers.*
import java.time.LocalDate

class PenaltyCalculationControllerISpec extends IntegrationSpecCommonBase with AuthWiremockStubs with PenaltiesWiremockStubs {

  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/calculation/XJ002616061027")).withSession(
    authToken -> "12345",
    SessionKeys.clientMTDID -> "987654321",
    SessionKeys.clientNino -> "AB123456A"
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
    mockDelegatedResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP.copy(
        penaltyAmountOutstanding = Some(0)
      )), true)),
      breathingSpace = None
    )

    mockGetPenaltyDetailsResponse(penaltyDetails = Some(getPenaltyDetailsPayload))

    val response = route(app, fakeAgentRequest).get
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

    select("p a").text shouldBe "Return to your client's Self Assessment penalties and appeals"
  }

  "return page with calculation (lower rate) when penalty is an estimate" in {
    mockDelegatedResponse()

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

    val response = route(app, fakeAgentRequest).get
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

    select("#main-content li:nth-child(1)").text shouldBe "your client pays their Income Tax bill, or"
    select("#main-content li:nth-child(2)").text shouldBe "30 days have passed since the Income Tax due date"

    select("p a").text shouldBe "Return to your client's Self Assessment penalties and appeals"
  }

}
