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

import org.apache.pekko.util.Timeout
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys.authToken
import utils.{AuthWiremockStubs, IntegrationSpecCommonBase}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

//class MockComponent extends AuthConnector {
//  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
//    successful(Some("").asInstanceOf[A])
//  }
//}

class PenaltiesControllerISpec extends IntegrationSpecCommonBase with AuthWiremockStubs {

//
//  class ComponentModule extends Module {
//    def bindings(env: Environment, conf: Configuration): Seq[Binding[_]] = Seq(
//      bind[AuthConnector].to[MockComponent]
//    )
//  }

//  override def fakeApplication(): Application = new GuiceApplicationBuilder()
////    .overrides(bind[AuthConnector].to[MockComponent])
//    .build()

  val fakeClientRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/")).withSession(
    authToken -> "12345"
  )

  val fakeAnonymousRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path("/"))

  implicit val timeout: Timeout = org.apache.pekko.util.Timeout(10 seconds)

  "GET /" should {
    "redirect to the login page when the user is not logged in" in {
      mockUnauthorisedResponse()
      val response = route(app, fakeAnonymousRequest).get
      redirectLocation(response)(timeout) shouldBe Some("http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9000%2Fincome-tax-penalties-frontend")
    }

    "return 200 (OK) when the user is authorised" in {
      mockEnroledResponse()
      //      getPenaltyDetailsStub()
      //      complianceDataStub()
//      val response = await(buildClientForRequestToApp(uri = "/").get)(timeout)
      //wireMockServer.addStubMapping()
      val response = await(route(app, fakeClientRequest).get)(timeout)
      response.header.status shouldBe Status.OK
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
