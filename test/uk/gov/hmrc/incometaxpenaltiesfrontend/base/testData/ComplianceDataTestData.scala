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

package uk.gov.hmrc.incometaxpenaltiesfrontend.base.testData

import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.{CompliancePayload, ComplianceStatusEnum, ObligationDetail, ObligationIdentification}

import java.time.LocalDate

trait ComplianceDataTestData {

  val sampleCompliancePayload: CompliancePayload = CompliancePayload(
    identification = Some(ObligationIdentification(
      incomeSourceType = None,
      referenceNumber = "123456789",
      referenceType = "VRN"
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

  val compliancePayloadObligationsFulfilled: CompliancePayload = sampleCompliancePayload.copy(
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
