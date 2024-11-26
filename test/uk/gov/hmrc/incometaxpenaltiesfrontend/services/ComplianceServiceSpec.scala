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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.fixtures.ComplianceDataTestData
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.ComplianceData
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ComplianceServiceSpec extends AnyWordSpec with Matchers with ComplianceDataTestData {

  val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
  val mtdItId: String = "12345678"

  class Setup {
    val service: ComplianceService = new ComplianceService(mockPenaltiesConnector)

    reset(mockPenaltiesConnector)
  }

  "getDESComplianceData" should {
    s"return a successful response and pass the result back to the controller (date provided as parameter)" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(),
        ArgumentMatchers.eq(LocalDate.of(2020, 1, 1)),
        ArgumentMatchers.eq(LocalDate.of(2022, 1, 1)))(any())).thenReturn(Future.successful(Right(sampleCompliancePayload)))

      val result: Option[ComplianceData] = await(service.getDESComplianceData(mtdItId, Some(LocalDate.of(2022, 1, 1)))(HeaderCarrier(), FakeRequest()))

      result.isDefined shouldBe true
      result.get shouldBe sampleCompliancePayload
    }

    s"return a successful response and pass the result back to the controller (date in session)" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(),
        ArgumentMatchers.eq(LocalDate.of(2020, 1, 1)),
        ArgumentMatchers.eq(LocalDate.of(2022, 1, 1)))(any())).thenReturn(Future.successful(Right(sampleCompliancePayload)))

      val result: Option[ComplianceData] = await(service.getDESComplianceData(mtdItId)(HeaderCarrier(),FakeRequest().withSession(
        IncomeTaxSessionKeys.pocAchievementDate -> "2022-01-01"
      )))

      result.isDefined shouldBe true
      result.get shouldBe sampleCompliancePayload
    }

    "return None when the session keys are not present" in new Setup {
      val result: Option[ComplianceData] = await(service.getDESComplianceData(mtdItId)(HeaderCarrier(),FakeRequest()))

      result shouldBe None
    }

    s"return an exception and pass the result back to the controller" in new Setup {
      when(mockPenaltiesConnector.getObligationData(any(), any(), any())(any()))
        .thenReturn(Future.failed(UpstreamErrorResponse.apply("Upstream error", INTERNAL_SERVER_ERROR)))

      val result: Exception = intercept[Exception](await(service.getDESComplianceData(mtdItId)(HeaderCarrier(), FakeRequest().withSession(
        IncomeTaxSessionKeys.pocAchievementDate -> "2022-01-01"
      ))))

      result.getMessage shouldBe "Upstream error"
    }
  }
}
