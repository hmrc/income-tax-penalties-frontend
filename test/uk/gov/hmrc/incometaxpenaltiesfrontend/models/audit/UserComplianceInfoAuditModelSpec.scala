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
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.mocks.IncomeTaxSessionMocks
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledIndividual, CurrentUserRequest}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.ITSAStatus

class UserComplianceInfoAuditModelSpec extends AnyWordSpec with Matchers with ComplianceDataTestData with IncomeTaxSessionMocks {

  "UserComplianceInfoAuditModel" should {

    "user is an Agent" should {

      val mtditid = "XA123456"
      val arn = "ARN123456"

      implicit val user: CurrentUserRequest[_] = AuthorisedAndEnrolledAgent(sessionData, Some(arn))(FakeRequest())

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
          "taxIdentifier" -> testMtdItId,
          "mandationStatus" -> "No Status",
          "complianceWindow" -> "2 days",
          "isPenaltyLate" -> true,
          "complianceData" -> Json.arr(
            Json.obj(
              "status" -> "Open",
              "inboundCorrespondenceFromDate" -> "2021-04-06",
              "inboundCorrespondenceToDate" -> "2022-04-05",
              "inboundCorrespondenceDueDate" -> "2023-01-31",
              "periodKey" -> "22P0"
            ),
            Json.obj(
              "status" -> "Fulfilled",
              "inboundCorrespondenceFromDate" -> "2022-04-06",
              "inboundCorrespondenceToDate" -> "2022-06-30",
              "inboundCorrespondenceDueDate" -> "2022-07-31",
              "inboundCorrespondenceDateReceived" -> "2022-07-01",
              "periodKey" -> "23P1"
            )
          )
        )
      }
    }

    "user is an Individual" should {

      val mtditid = "XA123456"

      implicit val user: CurrentUserRequest[_] = AuthorisedAndEnrolledIndividual(mtditid, "AA123456A", None)(FakeRequest())

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
              "inboundCorrespondenceFromDate" -> "2021-04-06",
              "inboundCorrespondenceToDate" -> "2022-04-05",
              "inboundCorrespondenceDueDate" -> "2023-01-31",
              "periodKey" -> "22P0"
            ),
            Json.obj(
              "status" -> "Fulfilled",
              "inboundCorrespondenceFromDate" -> "2022-04-06",
              "inboundCorrespondenceToDate" -> "2022-06-30",
              "inboundCorrespondenceDueDate" -> "2022-07-31",
              "inboundCorrespondenceDateReceived" -> "2022-07-01",
              "periodKey" -> "23P1"
            )
          )
        )
      }
    }
  }
}
