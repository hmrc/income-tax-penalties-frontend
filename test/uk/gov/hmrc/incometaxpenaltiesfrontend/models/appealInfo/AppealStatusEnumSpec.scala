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

class AppealStatusEnumSpec extends AnyWordSpec with Matchers {

  "AppealStatusEnum" should  {

    "deserialise from JSON" in {
      JsString("A").as[AppealStatusEnum.Value] shouldBe AppealStatusEnum.Under_Appeal
      JsString("B").as[AppealStatusEnum.Value] shouldBe AppealStatusEnum.Upheld
      JsString("C").as[AppealStatusEnum.Value] shouldBe AppealStatusEnum.Rejected
      JsString("99").as[AppealStatusEnum.Value] shouldBe AppealStatusEnum.Unappealable
    }

    "serialise to JSON" in {
      Json.toJson(AppealStatusEnum.Under_Appeal) shouldBe JsString("A")
      Json.toJson(AppealStatusEnum.Upheld) shouldBe JsString("B")
      Json.toJson(AppealStatusEnum.Rejected) shouldBe JsString("C")
      Json.toJson(AppealStatusEnum.Unappealable) shouldBe JsString("99")

    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[AppealStatusEnum.Value].isError shouldBe true
    }
  }

}
