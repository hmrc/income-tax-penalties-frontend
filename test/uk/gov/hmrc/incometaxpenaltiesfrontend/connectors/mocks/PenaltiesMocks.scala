/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.mocks

import fixtures.PenaltiesDetailsTestData
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.GetPenaltyDetailsParser.{GetPenaltyDetailsBadRequest, GetPenaltyDetailsUnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.CurrentUserRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.services.PenaltiesService

import scala.concurrent.Future

trait PenaltiesMocks extends MockFactory with PenaltiesDetailsTestData { _: TestSuite =>

  lazy val mockPenaltiesService: PenaltiesService = mock[PenaltiesService]

  def mockGetPenaltyDataForUser(): Unit =
    (mockPenaltiesService.getPenaltyDataForUser()(_: CurrentUserRequest[_], _:HeaderCarrier))
      .expects(*, *)
      .returning(
      Future.successful(Right(samplePenaltyDetailsModel))
    )

  def mockIncomeTaxSessionDataBadRequest(): Unit =
    (mockPenaltiesService.getPenaltyDataForUser()(_: CurrentUserRequest[_], _:HeaderCarrier))
      .expects(*,*)
      .returning(
        Future.successful(Left(GetPenaltyDetailsBadRequest))
      )

  def mockGetPenaltyDataInternalErrorRequest(): Unit =
    (mockPenaltiesService.getPenaltyDataForUser()(_: CurrentUserRequest[_], _:HeaderCarrier))
      .expects(*, *)
      .returning(
        Future.successful(Left(GetPenaltyDetailsUnexpectedFailure(500)))
      )

}
