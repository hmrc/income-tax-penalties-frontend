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

package controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.ServiceController
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndividualMainView
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.templates.SessionExpired

class ServiceControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  lazy val individualMainView: IndividualMainView = app.injector.instanceOf[IndividualMainView]
  lazy val sessionExpiredView: SessionExpired = app.injector.instanceOf[SessionExpired]

  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  object ServiceController extends ServiceController(stubMessagesControllerComponents(), individualMainView, sessionExpiredView)(appConfig)

  "GET /" should {
    "return 200" in {
      val result = ServiceController.individualMain(FakeRequest())
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = ServiceController.individualMain(FakeRequest())
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }
}
