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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.ITSAStatus.ITSAStatus

class ITSAStatusSpec extends AnyWordSpec with Matchers {

  "ITSAStatus" should  {

    "deserialise from JSON" in {
      JsString("No Status").as[ITSAStatus]     shouldBe ITSAStatus.NoStatus
      JsString("MTD Mandated").as[ITSAStatus]  shouldBe ITSAStatus.Mandated
      JsString("MTD Voluntary").as[ITSAStatus] shouldBe ITSAStatus.Voluntary
      JsString("Annual").as[ITSAStatus]        shouldBe ITSAStatus.Annual
      JsString("Non Digital").as[ITSAStatus]   shouldBe ITSAStatus.NonDigital
      JsString("Dormant").as[ITSAStatus]       shouldBe ITSAStatus.Dormant
      JsString("MTD Exempt").as[ITSAStatus]    shouldBe ITSAStatus.Exempt
    }

    "serialise to JSON" in {
      Json.toJson(ITSAStatus.NoStatus)    shouldBe JsString("No Status")
      Json.toJson(ITSAStatus.Mandated)    shouldBe JsString("MTD Mandated")
      Json.toJson(ITSAStatus.Voluntary)   shouldBe JsString("MTD Voluntary")
      Json.toJson(ITSAStatus.Annual)      shouldBe JsString("Annual")
      Json.toJson(ITSAStatus.NonDigital)  shouldBe JsString("Non Digital")
      Json.toJson(ITSAStatus.Dormant)     shouldBe JsString("Dormant")
      Json.toJson(ITSAStatus.Exempt)      shouldBe JsString("MTD Exempt")
    }
  }
}
