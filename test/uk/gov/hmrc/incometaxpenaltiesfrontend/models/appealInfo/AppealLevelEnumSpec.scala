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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.AppealLevelEnum

class AppealLevelEnumSpec extends AnyWordSpec with Matchers {

  "AppealLevelEnum" should  {

    "deserialise from JSON" in {
      JsString("01").as[AppealLevelEnum.Value] shouldBe AppealLevelEnum.FirstStageAppeal
      JsString("02").as[AppealLevelEnum.Value] shouldBe AppealLevelEnum.SecondStageAppeal
      JsString("03").as[AppealLevelEnum.Value] shouldBe AppealLevelEnum.Tribunal
    }

    "serialise to JSON" in {
      Json.toJson(AppealLevelEnum.FirstStageAppeal) shouldBe JsString("01")
      Json.toJson(AppealLevelEnum.SecondStageAppeal) shouldBe JsString("02")
      Json.toJson(AppealLevelEnum.Tribunal) shouldBe JsString("03")
    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[AppealLevelEnum.Value].isError shouldBe true
    }
  }



}
