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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPTypeEnum

class LSPTypeEnumSpec extends AnyWordSpec with Matchers{

  "LSPTypeEnum" should {

    "deserialize from JSON" in {
      JsString("AF").as[LSPTypeEnum.Value] shouldBe LSPTypeEnum.AddedFAP
      JsString("RF").as[LSPTypeEnum.Value] shouldBe LSPTypeEnum.RemovedFAP
      JsString("AP").as[LSPTypeEnum.Value] shouldBe LSPTypeEnum.AppealedPoint
      JsString("RP").as[LSPTypeEnum.Value] shouldBe LSPTypeEnum.RemovedPoint
      JsString("P").as[LSPTypeEnum.Value] shouldBe LSPTypeEnum.Point
      JsString("F").as[LSPTypeEnum.Value] shouldBe LSPTypeEnum.Financial
    }

    "serialise to JSON" in {
      Json.toJson(LSPTypeEnum.AddedFAP) shouldBe JsString("AF")
      Json.toJson(LSPTypeEnum.RemovedFAP) shouldBe JsString("RF")
      Json.toJson(LSPTypeEnum.AppealedPoint) shouldBe JsString("AP")
      Json.toJson(LSPTypeEnum.RemovedPoint) shouldBe JsString("RP")
      Json.toJson(LSPTypeEnum.Point) shouldBe JsString("P")
      Json.toJson(LSPTypeEnum.Financial) shouldBe JsString("F")
    }
  }
}
