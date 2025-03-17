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

package uk.gov.hmrc.incometaxpenaltiesfrontend.models.audit

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{CurrentUserRequest, PenaltyType}

class UserCalculationInfoAuditModelSpec extends AnyWordSpec with Matchers {

  "UserCalculationInfoAuditModel" should {

    "user is an Agent" should {

      val mtditid = "XA123456"
      val arn = "ARN123456"

      implicit val user: CurrentUserRequest[_] = CurrentUserRequest(mtditid, Some(arn))(FakeRequest())

      val model = UserCalculationInfoAuditModel(
        penaltyNumber = "123456",
        penaltyType = PenaltyType.LSP,
        penaltyTotalCost = 100.01,
        penaltyTotalPaid = 50.99
      )

      "have the correct audit detail" in {

        model.auditType shouldBe "UserCalculationInfo"
        model.detail shouldBe Json.obj(
          "agentReferenceNumber" -> arn,
          "identifierType" -> "MTDITID",
          "taxIdentifier" -> mtditid,
          "penaltyNumber" -> "123456",
          "penaltyType" -> "Late Submission Penalty",
          "penaltyTotalCost" -> 100.01,
          "penaltyTotalPaid" -> 50.99,
          "penaltyLeftToPay" -> 49.02
        )
      }
    }

    "user is an Individual" should {

      val mtditid = "XA123456"

      implicit val user: CurrentUserRequest[_] = CurrentUserRequest(mtditid)(FakeRequest())

      val model = UserCalculationInfoAuditModel(
        penaltyNumber = "123456",
        penaltyType = PenaltyType.LPP1,
        penaltyTotalCost = 100.01,
        penaltyTotalPaid = 100.01
      )

      "have the correct audit detail" in {

        model.auditType shouldBe "UserCalculationInfo"
        model.detail shouldBe Json.obj(
          "identifierType" -> "MTDITID",
          "taxIdentifier" -> mtditid,
          "penaltyNumber" -> "123456",
          "penaltyType" -> "Late Payment Penalty 1",
          "penaltyTotalCost" -> 100.01,
          "penaltyTotalPaid" -> 100.01,
          "penaltyLeftToPay" -> 0
        )
      }
    }
  }
}
