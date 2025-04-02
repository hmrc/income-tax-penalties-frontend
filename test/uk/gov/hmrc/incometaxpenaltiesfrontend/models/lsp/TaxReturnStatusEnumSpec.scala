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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lpp.LPPPenaltyStatusEnum

class TaxReturnStatusEnumSpec extends AnyWordSpec with Matchers{

  "TaxReturnStatusEnum" should {

    "deserialise from JSON" in {
      JsString("OPEN").as[TaxReturnStatusEnum.Value] shouldBe TaxReturnStatusEnum.Open
      JsString("FULFILLED").as[TaxReturnStatusEnum.Value] shouldBe TaxReturnStatusEnum.Fulfilled
      JsString("REVERSED").as[TaxReturnStatusEnum.Value] shouldBe TaxReturnStatusEnum.Reversed
    }

    "serialise to JSON" in {
      Json.toJson(TaxReturnStatusEnum.Open) shouldBe JsString("Open")
      Json.toJson(TaxReturnStatusEnum.Fulfilled) shouldBe JsString("Fulfilled")
      Json.toJson(TaxReturnStatusEnum.Reversed) shouldBe JsString("Reversed")
    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[LPPPenaltyStatusEnum.Value].isError shouldBe true
    }

  }



}
