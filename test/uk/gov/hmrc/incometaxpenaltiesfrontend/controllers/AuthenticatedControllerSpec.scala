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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.connectors.mocks.AuthMocks
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.IncomeTaxSessionKeys
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.auth.AuthenticatedController

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedControllerSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with AuthMocks {

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object TestController extends AuthenticatedController(stubMessagesControllerComponents())(ExecutionContext.global, appConfig) {

    val authConnector: AuthConnector = mockAuthConnector

    def callAuthenticated: Action[AnyContent] = isAuthenticated {
      _ =>
        _ =>
          Future.successful(Ok("ALL GOOD"))
    }

  }

  "isAuthenticated" should {
    "return 200 if user is Authenticated and has individual affinity" in {
      mockAuthenticatedIndividual()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe "ALL GOOD"
    }

    "return 200 if user is Authenticated and has agent affinity" in {
      mockAuthenticatedAgent()
      mockAuthenticatedAgentEnrolment("1234567890")

      val result = TestController.callAuthenticated(FakeRequest().withSession(IncomeTaxSessionKeys.agentSessionMtditid -> "1234567890"))
      status(result) shouldBe OK
      contentAsString(result) shouldBe "ALL GOOD"
    }

    "return 303 to GG login if user is Authenticated and has no affinity group" in {
      mockAuthenticatedWithNoAffinityGroup()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    "return 303 to GG login if user has No Active Session" in {
      mockAuthenticatedNoActiveSession()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
    }

    "return 303 to GG login if user is Not Authenticated" in {
      mockAuthenticatedFailure()

      val result = TestController.callAuthenticated(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in")
    }
  }

}
