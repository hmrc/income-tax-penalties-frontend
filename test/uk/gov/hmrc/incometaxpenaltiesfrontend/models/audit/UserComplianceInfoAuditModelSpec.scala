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

import fixtures.ComplianceDataTestData
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{CurrentUserRequest, ITSAStatus}

class UserComplianceInfoAuditModelSpec extends AnyWordSpec with Matchers with ComplianceDataTestData {

  "UserComplianceInfoAuditModel" should {

    "user is an Agent" should {

      val mtditid = "XA123456"
      val arn = "ARN123456"

      implicit val user: CurrentUserRequest[_] = CurrentUserRequest(mtditid, Some(arn))(FakeRequest())

      val model = UserComplianceInfoAuditModel(
        mandationStatus = ITSAStatus.NoStatus,
        complianceWindow = "2 days",
        isPenaltyLate = true,
        complianceData = obligationDetail
      )

      "have the correct audit detail" in {

        model.auditType shouldBe "UserComplianceInfo"
        model.detail shouldBe Json.obj(
          "agentReferenceNumber" -> arn,
          "identifierType" -> "MTDITID",
          "taxIdentifier" -> mtditid,
          "mandationStatus" -> "No Status",
          "complianceWindow" -> "2 days",
          "isPenaltyLate" -> true,
          "complianceData" -> Json.arr(
            Json.obj(
              "status" -> "Open",
              "inboundCorrespondenceFromDate" -> "1920-02-29",
              "inboundCorrespondenceToDate" -> "1920-02-29",
              "inboundCorrespondenceDueDate" -> "1920-02-29",
              "periodKey" -> "#001"
            ),
            Json.obj(
              "status" -> "Fulfilled",
              "inboundCorrespondenceFromDate" -> "1920-02-29",
              "inboundCorrespondenceToDate" -> "1920-02-29",
              "inboundCorrespondenceDueDate" -> "1920-02-29",
              "inboundCorrespondenceDateReceived" -> "1920-02-29",
              "periodKey" -> "#001"
            )
          )
        )
      }
    }

    "user is an Individual" should {

      val mtditid = "XA123456"

      implicit val user: CurrentUserRequest[_] = CurrentUserRequest(mtditid)(FakeRequest())

      val model = UserComplianceInfoAuditModel(
        mandationStatus = ITSAStatus.NoStatus,
        complianceWindow = "2 days",
        isPenaltyLate = true,
        complianceData = obligationDetail
      )

      "have the correct audit detail" in {

        model.auditType shouldBe "UserComplianceInfo"
        model.detail shouldBe Json.obj(
          "identifierType" -> "MTDITID",
          "taxIdentifier" -> mtditid,
          "mandationStatus" -> "No Status",
          "complianceWindow" -> "2 days",
          "isPenaltyLate" -> true,
          "complianceData" -> Json.arr(
            Json.obj(
              "status" -> "Open",
              "inboundCorrespondenceFromDate" -> "1920-02-29",
              "inboundCorrespondenceToDate" -> "1920-02-29",
              "inboundCorrespondenceDueDate" -> "1920-02-29",
              "periodKey" -> "#001"
            ),
            Json.obj(
              "status" -> "Fulfilled",
              "inboundCorrespondenceFromDate" -> "1920-02-29",
              "inboundCorrespondenceToDate" -> "1920-02-29",
              "inboundCorrespondenceDueDate" -> "1920-02-29",
              "inboundCorrespondenceDateReceived" -> "1920-02-29",
              "periodKey" -> "#001"
            )
          )
        )
      }
    }
  }
}
