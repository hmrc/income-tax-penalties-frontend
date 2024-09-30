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

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/"))

  val sampleLPP: LPPDetails = LPPDetails(
    penaltyChargeReference = Some("XJ002616061027"),
    principalChargeReference = "XJ002616061027",
    penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
    penaltyChargeCreationDate = None,
    penaltyStatus = LPPPenaltyStatusEnum.Posted,
    penaltyAmountPaid = Some(400.00),
    penaltyAmountPosted = 400.00,
    penaltyAmountAccruing = 0,
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
  }

  "return page with calculation - lower rate" in {
    mockEnroledResponse()

    val getPenaltyDetailsPayload = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = None,
      latePaymentPenalty = Some(LatePaymentPenalty(Seq(sampleLPP), true)),
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

    getElementsByClass("govuk-body").text shouldBe "This penalty applies if Income Tax has not been paid for 15 days."
    getElementById("paragraph2").text shouldBe "The calculation we use is: 2% of £20000.00 (the unpaid Income Tax 15 days after the due date)"

    getElementById("key1").text shouldBe "Penalty amount"
    getElementById("value1").text shouldBe "£400.00"

    getElementById("key2").text shouldBe "Amount received"
    getElementById("value2").text shouldBe "£400.00"

    getElementById("key3").text shouldBe "Left to pay"
    getElementById("value3").text shouldBe "£0.00"

    select("p a").text shouldBe "Return to Self Assessment penalties and appeals"
  }

}
