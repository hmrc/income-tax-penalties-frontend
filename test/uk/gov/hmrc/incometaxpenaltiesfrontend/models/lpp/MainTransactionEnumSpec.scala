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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.MainTransactionEnum

class MainTransactionEnumSpec extends AnyWordSpec with Matchers {

  "MainTransactionEnum" should  {

    "deserialise from JSON" in {
      JsString("4700").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ITSAReturnCharge
      JsString("4703").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ITSAReturnFirstLPP
      JsString("4704").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ITSAReturnSecondLPP
      JsString("4720").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.CentralAssessment
      JsString("4723").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.CentralAssessmentFirstLPP
      JsString("4724").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.CentralAssessmentSecondLPP
      JsString("4730").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.OfficersAssessment
      JsString("4741").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.OfficersAssessmentFirstLPP
      JsString("4742").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.OfficersAssessmentSecondLPP
      JsString("4731").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ErrorCorrection
      JsString("4743").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ErrorCorrectionFirstLPP
      JsString("4744").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ErrorCorrectionSecondLPP
      JsString("4732").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.AdditionalAssessment
      JsString("4758").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.AdditionalAssessmentFirstLPP
      JsString("4759").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.AdditionalAssessmentSecondLPP
      JsString("4733").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ProtectiveAssessment
      JsString("4761").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ProtectiveAssessmentFirstLPP
      JsString("4762").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ProtectiveAssessmentSecondLPP
      JsString("4764").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ITSAOverpaymentForTax
      JsString("4701").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.POAReturnCharge
      JsString("4716").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.POAReturnChargeFirstLPP
      JsString("4717").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.POAReturnChargeSecondLPP
      JsString("4702").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.AAReturnCharge
      JsString("4718").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.AAReturnChargeFirstLPP
      JsString("4719").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.AAReturnChargeSecondLPP
      JsString("4787").as[MainTransactionEnum.Value]  shouldBe MainTransactionEnum.ManualCharge
    }

    "serialise to JSON" in {
      Json.toJson(MainTransactionEnum.ITSAReturnCharge) shouldBe JsString("4700")
      Json.toJson(MainTransactionEnum.ITSAReturnFirstLPP) shouldBe JsString("4703")
      Json.toJson(MainTransactionEnum.ITSAReturnSecondLPP) shouldBe JsString("4704")
      Json.toJson(MainTransactionEnum.CentralAssessment) shouldBe JsString("4720")
      Json.toJson(MainTransactionEnum.CentralAssessmentFirstLPP) shouldBe JsString("4723")
      Json.toJson(MainTransactionEnum.CentralAssessmentSecondLPP) shouldBe JsString("4724")
      Json.toJson(MainTransactionEnum.OfficersAssessment) shouldBe JsString("4730")
      Json.toJson(MainTransactionEnum.OfficersAssessmentFirstLPP) shouldBe JsString("4741")
      Json.toJson(MainTransactionEnum.OfficersAssessmentSecondLPP) shouldBe JsString("4742")
      Json.toJson(MainTransactionEnum.ErrorCorrection) shouldBe JsString("4731")
      Json.toJson(MainTransactionEnum.ErrorCorrectionFirstLPP) shouldBe JsString("4743")
      Json.toJson(MainTransactionEnum.ErrorCorrectionSecondLPP) shouldBe JsString("4744")
      Json.toJson(MainTransactionEnum.AdditionalAssessment) shouldBe JsString("4732")
      Json.toJson(MainTransactionEnum.AdditionalAssessmentFirstLPP) shouldBe JsString("4758")
      Json.toJson(MainTransactionEnum.AdditionalAssessmentSecondLPP) shouldBe JsString("4759")
      Json.toJson(MainTransactionEnum.ProtectiveAssessment) shouldBe JsString("4733")
      Json.toJson(MainTransactionEnum.ProtectiveAssessmentFirstLPP) shouldBe JsString("4761")
      Json.toJson(MainTransactionEnum.ProtectiveAssessmentSecondLPP) shouldBe JsString("4762")
      Json.toJson(MainTransactionEnum.ITSAOverpaymentForTax) shouldBe JsString("4764")
      Json.toJson(MainTransactionEnum.POAReturnCharge) shouldBe JsString("4701")
      Json.toJson(MainTransactionEnum.POAReturnChargeFirstLPP) shouldBe JsString("4716")
      Json.toJson(MainTransactionEnum.POAReturnChargeSecondLPP) shouldBe JsString("4717")
      Json.toJson(MainTransactionEnum.AAReturnCharge) shouldBe JsString("4702")
      Json.toJson(MainTransactionEnum.AAReturnChargeFirstLPP) shouldBe JsString("4718")
      Json.toJson(MainTransactionEnum.AAReturnChargeSecondLPP) shouldBe JsString("4719")
      Json.toJson(MainTransactionEnum.ManualCharge) shouldBe JsString("4787")
    }

    "Unknown category should return jsError" in {
      JsString("INVALID").validate[MainTransactionEnum.Value].isError shouldBe true
    }
  }



}
