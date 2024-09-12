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

import connectors.PenaltiesConnector.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, lateSubmissionPenaltyFmt}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import java.time.LocalDate
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase, PenaltiesWiremockStubs}

class LPPISpec extends IntegrationSpecCommonBase with AuthWiremockStubs with PenaltiesWiremockStubs {

  val fakeClientRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/")).withSession(
    authToken -> "12345"
  )

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/"))

  val sampleMetaData: LPPDetailsMetadata = LPPDetailsMetadata(
    mainTransaction = None, outstandingAmount = None, timeToPay = None
  )

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
    vatOutstandingAmount = None,
    LPPDetailsMetadata = sampleMetaData
  )

  "GET /" should {
    "redirect to the login page when the user is not logged in" in {
      mockUnauthorisedResponse()
      val response = route(app, fakeAnonymousRequest).get
      redirectLocation(response) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9185%2Fincome-tax-penalties-frontend")
    }
  }
}

