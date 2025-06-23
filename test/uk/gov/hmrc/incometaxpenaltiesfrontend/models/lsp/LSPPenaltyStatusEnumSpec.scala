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

class LSPPenaltyStatusEnumSpec extends AnyWordSpec with Matchers{

  "LSPPenaltyStatusEnum" should {

    "deserialise from JSON" in {
      JsString("ACTIVE").as[LSPPenaltyStatusEnum.Value] shouldBe LSPPenaltyStatusEnum.Active
      JsString("INACTIVE").as[LSPPenaltyStatusEnum.Value] shouldBe LSPPenaltyStatusEnum.Inactive
    }

    "serialise to JSON" in {
      Json.toJson(LSPPenaltyStatusEnum.Active) shouldBe JsString("Active")
      Json.toJson(LSPPenaltyStatusEnum.Inactive) shouldBe JsString("Inactive")
    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[LPPPenaltyStatusEnum.Value].isError shouldBe true
    }

  }

}
