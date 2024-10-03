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

package controllers.testOnly

import base.SpecBase
import com.google.inject
import connectors.SessionDataConnector
import connectors.SessionDataConnector.SessionData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.Future.successful
import scala.reflect.ClassTag

class SetDelegationControllerSpec extends SpecBase {

  val mockSessionDataConnector = Mockito.mock(classOf[SessionDataConnector])

  override protected def applicationBuilder(): GuiceApplicationBuilder = super.applicationBuilder()
    .overrides(
      bindInstance(mockSessionDataConnector)
    ).configure(
      "application.router" -> "testOnlyDoNotUseInAppConf.Routes",
    )

  "Delegation Page" - {
    "must example use cookies if not using session service " in {
      val application = applicationBuilder().configure("feature.useSessionService" -> false).build()

      running(application) {
        val request = FakeRequest(GET, routes.SetDelegationController.delegationPage().url)

        val result = route(application, request).value

        status(result) mustEqual OK

        verifyNoInteractions(mockSessionDataConnector)
      }
    }

    "must use session service if not using cookies" in {
      val application = applicationBuilder().configure("feature.useSessionService" -> true).build()
      when(mockSessionDataConnector.getSessionData(any())).thenReturn(successful(SessionData(sessionId = Some("foo"))))

      running(application) {
        val request = FakeRequest(GET, routes.SetDelegationController.delegationPage().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        verify(mockSessionDataConnector).getSessionData(any())
        verifyNoMoreInteractions(mockSessionDataConnector)
      }
    }

  }
}
