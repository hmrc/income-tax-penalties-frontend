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

import fixtures.PenaltiesDetailsTestData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.PenaltiesConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.{GetPenaltyDetailsBadRequest, GetPenaltyDetailsMalformed, GetPenaltyDetailsUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.{CurrentUserRequest, PenaltyDetails}

import scala.concurrent.Future

class PenaltiesServiceSpec extends AnyWordSpec with Matchers with PenaltiesDetailsTestData with GuiceOneAppPerSuite {

  class Setup {

    implicit val userRequest: CurrentUserRequest[AnyContentAsEmpty.type] = CurrentUserRequest("1234567890")(FakeRequest())
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockPenaltiesConnector: PenaltiesConnector = mock(classOf[PenaltiesConnector])
    val service: PenaltiesService = new PenaltiesService(mockPenaltiesConnector)
    val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
    reset(mockPenaltiesConnector)
  }

  "getPenaltyDataFromEnrolmentKey" when  {
    s"$OK (Ok) is returned from the parser " should {
      "return a Right with the correct model" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(any(), any())(any()))
          .thenReturn(Future.successful(Right(samplePenaltyDetailsModel)))

        val result = await(service.getPenaltyDataForUser())
        
        result.isRight shouldBe true
        result shouldBe Right(samplePenaltyDetailsModel)
      }
    }

    s"$NO_CONTENT (No content) is returned from the parser" should {
      "return an empty Right PenaltyDetails model" in new Setup {
        when(mockPenaltiesConnector.getPenaltyDetails(any(), any())(any()))
          .thenReturn(Future.successful(Right(PenaltyDetails(None, None, None, None))))

        val result = await(service.getPenaltyDataForUser())
        result.isRight shouldBe true
        result shouldBe Right(PenaltyDetails(None, None, None, None))
      }

      s"$BAD_REQUEST (Bad request) is returned from the parser because of invalid json" should {
        "return a Left with status 400" in new Setup {
          when(mockPenaltiesConnector.getPenaltyDetails(any(), any())(any()))
            .thenReturn(Future.successful(Left(GetPenaltyDetailsMalformed)))

          val result = await(service.getPenaltyDataForUser())

          result.isLeft shouldBe true
          result shouldBe Left(GetPenaltyDetailsMalformed)
        }
      }

      s"$BAD_REQUEST (Bad request) is returned from the parser" should {
        "return a Left with status 400" in new Setup {
          when(mockPenaltiesConnector.getPenaltyDetails(any(), any())(any()))
            .thenReturn(Future.successful(Left(GetPenaltyDetailsBadRequest)))

          val result = await(service.getPenaltyDataForUser())
          result.isLeft shouldBe true
          result shouldBe Left(GetPenaltyDetailsBadRequest)
        }
      }

      s"an unexpected error is returned from the parser" should {
        "return a Left with the status and message" in new Setup {
          when(mockPenaltiesConnector.getPenaltyDetails(any(), any())(any()))
            .thenReturn(Future.successful(Left(GetPenaltyDetailsUnexpectedFailure(INTERNAL_SERVER_ERROR))))

          val result = await(service.getPenaltyDataForUser())
          result.isLeft shouldBe true
          result shouldBe Left(GetPenaltyDetailsUnexpectedFailure(INTERNAL_SERVER_ERROR))
        }
      }
    }
  }
}
