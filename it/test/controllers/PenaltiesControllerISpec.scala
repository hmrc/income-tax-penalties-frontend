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

import config.AppConfig
import org.apache.pekko.util.Timeout
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, status}
import uk.gov.hmrc.http.SessionKeys
import utils.IntegrationSpecCommonBase

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class PenaltiesControllerISpec extends IntegrationSpecCommonBase {

//  val controller: PenaltiesController = injector.instanceOf[PenaltiesController]

  val appConfig: AppConfig = injector.instanceOf[AppConfig]

//  val controller: IndexController = injector.instanceOf[IndexController]
  val fakeAgentRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
//    SessionKeys.agentSessionVrn -> "123456789",
//    authToken -> "12345",
//    SessionKeys.pocAchievementDate -> "2022-01-01",
//    SessionKeys.regimeThreshold -> "5"
  )
  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withSession(
//    authToken -> "12345",
//    SessionKeys.pocAchievementDate -> "2022-01-01",
//    SessionKeys.regimeThreshold -> "5"
  )
  implicit val timeout: Timeout = org.apache.pekko.util.Timeout(10 seconds)

  "GET /" should {
    "return 200 (OK) when the user is authorised" in {
      //      getPenaltyDetailsStub()
      //      complianceDataStub()
      val response = await(buildClientForRequestToApp(uri = "/").get)
      response.status shouldBe Status.OK
    }


    //  s"return OK" when {
    //    "the get penalty details call is successful" in {
    //
    //    }
    //  }
    //
    //  s"return NOT_FOUND" when {
    //    "the get penalty details call is unable to retrieve the data" in {
    //
    //    }
    //  }
    //
    //  s"return INTERNAL_SERVER_ERROR" when {
    //    "the get penalty details call fails" in {
    //
    //    }
    //  }
  }
}
