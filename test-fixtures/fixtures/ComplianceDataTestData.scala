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

package fixtures

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.{ComplianceData, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}

import java.time.LocalDate

trait ComplianceDataTestData {

  val sampleCompliancePayload: ComplianceData = ComplianceData(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "1234567890",
      referenceType = "MTDITID"
    )),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2021, 4, 6),
        inboundCorrespondenceToDate = LocalDate.of(2022, 4, 5),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2023, 1, 31),
        periodKey = "22P0"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 6),
        inboundCorrespondenceToDate = LocalDate.of(2022, 6, 30),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 7, 1)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 7, 31),
        periodKey = "23P1"
      )
    )
  )

  val sampleCompliancePayloadTwoOpen: ComplianceData = ComplianceData(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "1234567890",
      referenceType = "MTDITID"
    )),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2021, 4, 6),
        inboundCorrespondenceToDate = LocalDate.of(2022, 4, 5),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2023, 1, 31),
        periodKey = "22P0"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 6),
        inboundCorrespondenceToDate = LocalDate.of(2022, 6, 30),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 7, 1)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 7, 31),
        periodKey = "23P1"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 7, 1),
        inboundCorrespondenceToDate = LocalDate.of(2022, 9, 30),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(2022, 10, 31),
        periodKey = "23P2"
      )
    )
  )

  val compliancePayloadObligationsFulfilled: ComplianceData = sampleCompliancePayload.copy(
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2021, 4, 6),
        inboundCorrespondenceToDate = LocalDate.of(2022, 4, 5),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2023, 1, 30)),
        inboundCorrespondenceDueDate = LocalDate.of(2023, 1, 31),
        periodKey = "22P0"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(2022, 4, 6),
        inboundCorrespondenceToDate = LocalDate.of(2022, 6, 30),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(2022, 7, 1)),
        inboundCorrespondenceDueDate = LocalDate.of(2022, 7, 31),
        periodKey = "23P1"
      )
    )
  )
}
