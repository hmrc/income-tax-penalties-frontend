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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.ExpiryReasonEnum

class ExpiryReasonEnumSpec extends AnyWordSpec with Matchers{

  "ExpiryReasonEnum" should  {

    "deserialise from JSON" in {
      JsString("APP").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.Appeal
      JsString("FAP").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.Adjustment
      JsString("ICR").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.Reversal
      JsString("MAN").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.Manual
      JsString("NAT").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.NaturalExpiration
      JsString("NLT").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.SubmissionOnTime
      JsString("POC").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.Compliance
      JsString("RES").as[ExpiryReasonEnum.Value]  shouldBe ExpiryReasonEnum.Reset
    }

    "serialise to JSON" in {
      Json.toJson(ExpiryReasonEnum.Appeal) shouldBe JsString("APP")
      Json.toJson(ExpiryReasonEnum.Adjustment) shouldBe JsString("FAP")
      Json.toJson(ExpiryReasonEnum.Reversal) shouldBe JsString("ICR")
      Json.toJson(ExpiryReasonEnum.Manual) shouldBe JsString("MAN")
      Json.toJson(ExpiryReasonEnum.NaturalExpiration) shouldBe JsString("NAT")
      Json.toJson(ExpiryReasonEnum.SubmissionOnTime) shouldBe JsString("NLT")
      Json.toJson(ExpiryReasonEnum.Compliance) shouldBe JsString("POC")
      Json.toJson(ExpiryReasonEnum.Reset) shouldBe JsString("RES")
    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[LPPPenaltyStatusEnum.Value].isError shouldBe true
    }
  }



}
