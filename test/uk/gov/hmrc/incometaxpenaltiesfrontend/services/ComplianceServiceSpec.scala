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

package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import fixtures.ComplianceDataTestData
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ComplianceData

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ComplianceServiceSpec extends AnyWordSpec with Matchers with ComplianceDataTestData {

  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
  val nino = "AA123456A"

  class Setup {
    val service: ComplianceService = new ComplianceService(mockPenaltiesConnector)
    reset(mockPenaltiesConnector)
  }

  "getDESComplianceData" should {
    s"return a successful response and pass the result back to the controller" in new Setup {

      when(mockPenaltiesConnector.getComplianceData(any(),
        ArgumentMatchers.eq(LocalDate.of(2020, 1, 1)),
        ArgumentMatchers.eq(LocalDate.of(2022, 1, 1)))(any())).thenReturn(Future.successful(Right(sampleCompliancePayload)))

      val result: Option[ComplianceData] = await(service.getDESComplianceData(
        nino = nino,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2022, 1, 1)
      )(HeaderCarrier()))

      result.isDefined shouldBe true
      result.get shouldBe sampleCompliancePayload
    }

    s"return an exception and pass the result back to the controller" in new Setup {
      when(mockPenaltiesConnector.getComplianceData(any(), any(), any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result: Exception = intercept[Exception](await(service.getDESComplianceData(
        nino = nino,
        startDate = LocalDate.of(2020, 1, 1),
        endDate = LocalDate.of(2022, 1, 1)
      )(HeaderCarrier())))

      result.getMessage shouldBe "Upstream error"
    }
  }
}
