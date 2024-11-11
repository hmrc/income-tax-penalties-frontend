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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views


import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndividualMainView
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}


class IndividualMainViewSpec extends AnyWordSpec
  with Matchers
  with GuiceOneAppPerSuite {



  private val individualMainView = app.injector.instanceOf[IndividualMainView]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  "individualMainView" should {

    "have the correct title" in {
      val result = individualMainView()
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
    }

    "have the correct page heading" in {
      val result = individualMainView()
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementsByTag("h1").text shouldBe "Self Assessment penalties and appeals"
    }

    "have the correct page has correct elements" in {
      val result = individualMainView()
      val doc = Jsoup.parse(contentAsString(result))
      doc.getElementById("overviewHeading").text shouldBe "Overview"
      doc.getElementById("checkAmountPayButton").text shouldBe "Check amounts and pay"
      doc.getElementById("appealDetailsHeading").text shouldBe "Penalty and appeal details"
      doc.getElementById("lspHeading").text shouldBe "Late submission penalties"
      doc.getElementById("lppHeading").text shouldBe "Late payment penalties"
    }


  }

}
