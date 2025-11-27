/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.utils

import fixtures.BtaNavContentFixture
import org.jsoup.Jsoup
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.{AuthStub, BtaNavLinksStub, IncomeTaxSessionDataStub, MessagesStub}

trait NavBarTesterHelper extends AnyWordSpec with BtaNavLinksStub with MessagesStub with BtaNavContentFixture { this: ComponentSpecHelper with AuthStub with IncomeTaxSessionDataStub =>
  val nino = "AA123456A"
  def testNavBar(url: String, queryParams: Map[String, String] = Map.empty)(runStubs: => Unit = ()): Unit = {
    "Checking the Navigation Bar" when {
      "the origin is PTA" should {
        "render the PTA content" in {
          stubAuth(OK, successfulIndividualAuthResponse(nino))
          stubMessagesCount()(OK, Json.obj("count" -> 0))
          runStubs
          val result = get(url, origin = Some("PTA"), queryParams = queryParams)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav.hmrc-account-menu").isEmpty shouldBe false
        }
      }

      "the origin is BTA" should {
        "render the BTA content" in {
          stubAuth(OK, successfulIndividualAuthResponse(nino))
          runStubs
          stubBtaNavLinks()(OK, Json.toJson(btaNavContent))
          val result = get(url, origin = Some("BTA"), queryParams = queryParams)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav-bta.hmrc-account-menu").isEmpty shouldBe false
        }
      }

      "the origin is unknown" should {
        "render without a Nav" in {
          stubAuth(OK, successfulIndividualAuthResponse(nino))
          runStubs
          val result = get(url, origin = None, queryParams = queryParams)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)

          document.select("nav#secondary-nav-bta.hmrc-account-menu").isEmpty shouldBe true
          document.select("nav#secondary-nav.hmrc-account-menu").isEmpty shouldBe true
        }
      }
    }
  }

  def testNoNavBar(url: String, queryParams: Map[String, String] = Map.empty)(runStubs: => Unit = ()): Unit = {
    "the user is an Agent" should {
      "render without a Nav" in {
        stubAuth(OK, successfulAgentAuthResponse)
        stubGetIncomeTaxSessionDataSuccessResponse(nino)
        runStubs
        val result = get(url, origin = Some("BTA"), isAgent = true, queryParams = queryParams)

        result.status shouldBe OK
        val document = Jsoup.parse(result.body)

        document.select("nav#secondary-nav-bta.hmrc-account-menu").isEmpty shouldBe true
        document.select("nav#secondary-nav.hmrc-account-menu").isEmpty shouldBe true
      }
    }
  }
}
