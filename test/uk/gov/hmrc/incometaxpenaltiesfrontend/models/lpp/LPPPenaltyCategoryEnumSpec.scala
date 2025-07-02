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
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyCategoryEnum

class LPPPenaltyCategoryEnumSpec extends AnyWordSpec with Matchers {

  "LPPPenaltyCategoryEnum" should  {

    "deserialise from JSON" in {
      JsString("LPP1").as[LPPPenaltyCategoryEnum.Value]  shouldBe LPPPenaltyCategoryEnum.LPP1
      JsString("LPP2").as[LPPPenaltyCategoryEnum.Value]  shouldBe LPPPenaltyCategoryEnum.LPP2
      JsString("MANUAL").as[LPPPenaltyCategoryEnum.Value]  shouldBe LPPPenaltyCategoryEnum.MANUAL
    }

    "serialise to JSON" in {
      Json.toJson(LPPPenaltyCategoryEnum.LPP1) shouldBe JsString("LPP1")
      Json.toJson(LPPPenaltyCategoryEnum.LPP2) shouldBe JsString("LPP2")
      Json.toJson(LPPPenaltyCategoryEnum.MANUAL) shouldBe JsString("MANUAL")
    }

    "find Should return Some for LPP1 and LPP2 and None for Manual" in {
      LPPPenaltyCategoryEnum.find("LPP1") shouldBe Some(LPPPenaltyCategoryEnum.LPP1)
      LPPPenaltyCategoryEnum.find("LPP2") shouldBe Some(LPPPenaltyCategoryEnum.LPP2)
      LPPPenaltyCategoryEnum.find("MANUAL") shouldBe None
    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[LPPPenaltyCategoryEnum.Value].isError shouldBe true
    }
  }

}
