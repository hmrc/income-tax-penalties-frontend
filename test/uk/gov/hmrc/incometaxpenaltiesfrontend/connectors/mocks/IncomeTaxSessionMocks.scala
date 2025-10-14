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

import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.IncomeTaxSessionDataConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.httpParsers.{BadRequest, UnexpectedFailure}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.SessionData

import scala.concurrent.{ExecutionContext, Future}

trait IncomeTaxSessionMocks extends MockFactory {
  _: TestSuite =>

  lazy val mockSessionDataConnector: IncomeTaxSessionDataConnector = mock[IncomeTaxSessionDataConnector]

  val testMtdItId = "123456789"
  val testNino = "AA123456A"
  val testUtr = "9999912345"

  val sessionData: SessionData = SessionData(
    mtditid = testMtdItId,
    nino = testNino,
    utr = testUtr
  )

  def mockIncomeTaxSessionDataFound(): Unit =
    (mockSessionDataConnector.getSessionData()(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(Right(Some(sessionData))))

  def mockIncomeTaxSessionDataNotFound(): Unit =
    (mockSessionDataConnector.getSessionData()(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(Right(None)))

  def mockIncomeTaxSessionDataBadRequest(): Unit =
    (mockSessionDataConnector.getSessionData()(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(Left(BadRequest)))

  def mockIncomeTaxSessionDataInternalErrorRequest(): Unit =
    (mockSessionDataConnector.getSessionData()(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(Left(UnexpectedFailure(500, "error"))))

}
