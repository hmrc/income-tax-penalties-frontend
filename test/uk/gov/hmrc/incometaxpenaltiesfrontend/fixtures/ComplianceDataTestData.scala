/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.fixtures

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
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 2, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      )
    )
  )

  val sampleCompliancePayloadTwoEvents: ComplianceData = ComplianceData(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "1234567890",
      referenceType = "MTDITID"
    )),
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Open,
        inboundCorrespondenceFromDate = LocalDate.of(1921, 2, 20),
        inboundCorrespondenceToDate = LocalDate.of(1921, 2, 20),
        inboundCorrespondenceDateReceived = None,
        inboundCorrespondenceDueDate = LocalDate.of(1921, 2, 20),
        periodKey = "#002"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 2, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      )
    )
  )

  val compliancePayloadObligationsFulfilled: ComplianceData = sampleCompliancePayload.copy(
    obligationDetails = Seq(
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 2, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      ),
      ObligationDetail(
        status = ComplianceStatusEnum.Fulfilled,
        inboundCorrespondenceFromDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceToDate = LocalDate.of(1920, 2, 29),
        inboundCorrespondenceDateReceived = Some(LocalDate.of(1920, 2, 29)),
        inboundCorrespondenceDueDate = LocalDate.of(1920, 2, 29),
        periodKey = "#001"
      ),
    )
  )
}
