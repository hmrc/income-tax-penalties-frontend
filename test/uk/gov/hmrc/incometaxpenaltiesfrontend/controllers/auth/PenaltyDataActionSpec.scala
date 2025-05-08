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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth

import org.scalatest.Assertion
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.Ok
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.mocks.PenaltiesMocks
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.PenaltyDataAction
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.{AuthenticatedUserWithPenaltyData, AuthorisedAndEnrolledIndividual}

import scala.concurrent.Future

class PenaltyDataActionSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite
  with PenaltiesMocks {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  def blockWithAssertion(requestTestCase: AuthenticatedUserWithPenaltyData[_] => Assertion
                        ): AuthenticatedUserWithPenaltyData[_] => Future[Result] = testRequest => {
    requestTestCase(testRequest)
    Future.successful(Results.Ok("ALL GOOD"))
  }

  val block: AuthenticatedUserWithPenaltyData[_] => Future[Result] = { _ => Future.successful(Ok("ALL GOOD")) }

  val testAction = new PenaltyDataAction(
    penaltiesService = mockPenaltiesService,
    errorHandler = errorHandler,
    mcc = stubMessagesControllerComponents()
  )

  lazy val authorisedAndEnrolledIndividualRequest: AuthorisedAndEnrolledIndividual[_] = AuthorisedAndEnrolledIndividual("mtditid", "AA123456A", None)(FakeRequest())

  ".refine()" when {
    "the user has penalty details" should {
      "return the AuthenticatedUserWithPenaltyData request" in {
        mockGetPenaltyDataForUser()
        val result = testAction.invokeBlock(authorisedAndEnrolledIndividualRequest, blockWithAssertion(
          res => {
            res.penaltyDetails shouldBe samplePenaltyDetailsModel
          }
        ))
        status(result) shouldBe OK
        contentAsString(result) shouldBe "ALL GOOD"
      }
    }

    "getting penalty details returns an error" should {
      "render the error page" in {
        mockGetPenaltyDataInternalErrorRequest()
        val result = testAction.invokeBlock(authorisedAndEnrolledIndividualRequest, block)
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
