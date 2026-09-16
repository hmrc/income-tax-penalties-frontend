/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessagesControllerComponents
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.AppealsController
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.actions.AuthActions
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.models.{AuthorisedAndEnrolledAgent, AuthorisedAndEnrolledIndividual, CurrentUserRequest, SessionData}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import org.mockito.ArgumentMatchers.{eq => meq}
import play.api.mvc.{ActionBuilder, AnyContent, BodyParser, Request, Result}
import play.api.test.Helpers._
import scala.concurrent.{ExecutionContext, Future}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.when

class AppealsControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks with MockitoSugar {

  val mockAuthActions: AuthActions = mock[AuthActions]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  lazy val dateFormatter: DateFormatter = DateFormatter
  val mcc: MessagesControllerComponents = stubMessagesControllerComponents()
  lazy val currentUserRequest: CurrentUserRequest[_] = AuthorisedAndEnrolledIndividual(mtdItId = "testMtdItId", nino = "testNino", navBar = None)(FakeRequest())
  lazy val currentUserRequestAgent: CurrentUserRequest[_] = AuthorisedAndEnrolledAgent(sessionData = SessionData(mtditid = "testMtdItId", nino = "testNino", utr = "testUtr"), arn = Some("1"))(FakeRequest())

  val testAction = new AppealsController(
    authActions = mockAuthActions,
    controllerComponents = mcc
  )(appConfig)

  def stubAsMTDUser(isAgent: Boolean, request: CurrentUserRequest[_]): Unit = {
    when(mockAuthActions.asMTDUser(meq(isAgent))).thenReturn(
      new ActionBuilder[CurrentUserRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = mcc.executionContext

        override def invokeBlock[A](req: Request[A], block: CurrentUserRequest[A] => Future[Result]): Future[Result] =
          block(request.asInstanceOf[CurrentUserRequest[A]])

      }
    )
  }

  "redirect To Appeals" should {
    "redirect to the appeals frontend with the correct query params for a non-agent" in {

      stubAsMTDUser(isAgent = false, currentUserRequest)

      val result = testAction.redirectToAppeals(
        penaltyId = "1234",
        isAgent = false,
        isLPP = true,
        isFindOutHowToAppealLSP = false,
        isLPP2 = true,
        is2ndStageAppeal = false
      )(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(s"${appConfig.incomeTaxPenaltiesAppealsBaseUrl}/initialise-appeal?penaltyId=1234&isAgent=false&isLPP=true&isAdditional=true&is2ndStageAppeal=false")
    }
  }
}
