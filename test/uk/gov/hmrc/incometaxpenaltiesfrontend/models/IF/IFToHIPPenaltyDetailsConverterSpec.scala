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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.IF

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.IF.IFToHIPPenaltyDetailsConverter

import java.time.LocalDate

class IFToHIPPenaltyDetailsConverterSpec extends AnyWordSpec with Matchers {

  "penaltyDetailsReads" should {

    // Regression test: timeToPay arrives as a JSON array from IF, not a plain object.
    // Previously `lppMetadataReads` called `validateOpt[TimeToPay]` which expected a
    // JsObject and produced:
    //   JsError(List(((0), List(JsonValidationError(List(error.expected.jsobject), ...)))))
    "parse an LPP detail whose timeToPay field is a JSON array" in {
      val json = Json.parse(
        """{
          |  "latePaymentPenalty": {
          |    "details": [
          |      {
          |        "penaltyCategory": "LPP1",
          |        "penaltyChargeReference": "XH002616098919",
          |        "principalChargeReference": "XQ002610233429",
          |        "penaltyChargeCreationDate": "2026-07-24",
          |        "penaltyStatus": "P",
          |        "principalChargeBillingFrom": "2024-04-06",
          |        "principalChargeBillingTo": "2025-04-05",
          |        "principalChargeDueDate": "2026-01-31",
          |        "penaltyAmountOutstanding": 800,
          |        "penaltyAmountPosted": 800,
          |        "penaltyAmountAccruing": 0,
          |        "LPP1LRDays": "16",
          |        "LPP1HRDays": "30",
          |        "LPP1HRCalculationAmount": 20000,
          |        "LPP1LRCalculationAmount": 20000,
          |        "LPP1LRPercentage": 2,
          |        "LPP1HRPercentage": 2,
          |        "penaltyChargeDueDate": "2026-04-05",
          |        "principalChargeMainTransaction": "4910",
          |        "vatOutstandingAmount": 20000,
          |        "supplement": false,
          |        "mainTransaction": "4910",
          |        "timeToPay": [
          |          {
          |            "TTPAgreementDate": "2026-08-08"
          |          }
          |        ],
          |        "principalChargeDocNumber": "454000000636"
          |      }
          |    ]
          |  }
          |}""".stripMargin
      )

      val result = IFToHIPPenaltyDetailsConverter.penaltyDetailsReads.reads(json)

      result.isSuccess shouldBe true

      val lppDetails = result.get.latePaymentPenalty.flatMap(_.lppDetails).getOrElse(fail("no LPP details"))
      lppDetails should have size 1

      val detail = lppDetails.head
      detail.ttpAgreementDate shouldBe Some(LocalDate.of(2026, 8, 8))
      detail.ttpProposalDate  shouldBe None
    }

    "parse an LPP detail when timeToPay is absent" in {
      val json = Json.parse(
        """{
          |  "latePaymentPenalty": {
          |    "details": [
          |      {
          |        "penaltyCategory": "LPP1",
          |        "penaltyChargeReference": "XH002616098919",
          |        "principalChargeReference": "XQ002610233429",
          |        "penaltyChargeCreationDate": "2026-07-24",
          |        "penaltyStatus": "P",
          |        "principalChargeBillingFrom": "2024-04-06",
          |        "principalChargeBillingTo": "2025-04-05",
          |        "principalChargeDueDate": "2026-01-31",
          |        "penaltyAmountOutstanding": 800,
          |        "penaltyAmountPosted": 800,
          |        "penaltyAmountAccruing": 0,
          |        "mainTransaction": "4910"
          |      }
          |    ]
          |  }
          |}""".stripMargin
      )

      val result = IFToHIPPenaltyDetailsConverter.penaltyDetailsReads.reads(json)

      result.isSuccess shouldBe true

      val detail = result.get.latePaymentPenalty.flatMap(_.lppDetails).getOrElse(fail("no LPP details")).head
      detail.ttpAgreementDate shouldBe None
      detail.ttpProposalDate  shouldBe None
    }

    "parse an LPP detail when timeToPay array contains a TTPProposalDate" in {
      val json = Json.parse(
        """{
          |  "latePaymentPenalty": {
          |    "details": [
          |      {
          |        "penaltyCategory": "LPP1",
          |        "principalChargeReference": "XQ002610233429",
          |        "penaltyStatus": "P",
          |        "principalChargeBillingFrom": "2024-04-06",
          |        "principalChargeBillingTo": "2025-04-05",
          |        "principalChargeDueDate": "2026-01-31",
          |        "penaltyAmountPosted": 800,
          |        "penaltyAmountAccruing": 0,
          |        "mainTransaction": "4910",
          |        "timeToPay": [
          |          {
          |            "TTPProposalDate": "2026-09-01"
          |          }
          |        ]
          |      }
          |    ]
          |  }
          |}""".stripMargin
      )

      val result = IFToHIPPenaltyDetailsConverter.penaltyDetailsReads.reads(json)

      result.isSuccess shouldBe true

      val detail = result.get.latePaymentPenalty.flatMap(_.lppDetails).getOrElse(fail("no LPP details")).head
      detail.ttpProposalDate  shouldBe Some(LocalDate.of(2026, 9, 1))
      detail.ttpAgreementDate shouldBe None
    }
  }
}

