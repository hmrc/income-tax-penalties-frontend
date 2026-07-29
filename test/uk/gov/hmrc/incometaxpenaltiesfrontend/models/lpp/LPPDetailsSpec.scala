/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPDetails

import java.time.LocalDate

class LPPDetailsSpec extends AnyWordSpec with Matchers {

  "LPPDetails.format reads" should {

    // Regression: timeToPay arrives from IF as an array, not a plain object.
    // Previously `LPPDetailsMetadata.format` used Json.format which expected a JsObject
    // and produced: JsError(List(((0), List(JsonValidationError(List(error.expected.jsobject), ...)))))
    "parse timeToPay when supplied as a JSON array (IF format)" in {
      val json = Json.parse(
        """{
          |  "penaltyCategory": "LPP1",
          |  "principalChargeReference": "XQ002610233429",
          |  "penaltyStatus": "P",
          |  "principalChargeBillingFrom": "2024-04-06",
          |  "principalChargeBillingTo": "2025-04-05",
          |  "principalChargeDueDate": "2026-01-31",
          |  "penaltyAmountPosted": 800,
          |  "penaltyAmountAccruing": 0,
          |  "principalChargeMainTr": "4910",
          |  "timeToPay": [
          |    { "TTPAgreementDate": "2026-08-08" }
          |  ]
          |}""".stripMargin
      )

      val result = LPPDetails.format.reads(json)

      result.isSuccess shouldBe true
      result.get.ttpAgreementDate shouldBe Some(LocalDate.of(2026, 8, 8))
      result.get.ttpProposalDate  shouldBe None
    }

    "return None for timeToPay when the field is absent" in {
      val json = Json.parse(
        """{
          |  "penaltyCategory": "LPP1",
          |  "principalChargeReference": "XQ002610233429",
          |  "penaltyStatus": "P",
          |  "principalChargeBillingFrom": "2024-04-06",
          |  "principalChargeBillingTo": "2025-04-05",
          |  "principalChargeDueDate": "2026-01-31",
          |  "penaltyAmountPosted": 800,
          |  "penaltyAmountAccruing": 0,
          |  "principalChargeMainTr": "4910"
          |}""".stripMargin
      )

      val result = LPPDetails.format.reads(json)

      result.isSuccess shouldBe true
      result.get.ttpAgreementDate shouldBe None
      result.get.ttpProposalDate  shouldBe None
    }
  }
}

