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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views

import fixtures.messages.IndexViewMessages
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndexView

class IndexViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures {

  lazy val indexView: IndexView = app.injector.instanceOf[IndexView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  object Selectors {
    val lspTab = "#lspTab"
    val lppTab = "#lppTab"
  }

  "indexView" when {

    Seq(IndexViewMessages.English, IndexViewMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"rendering in the language '${messagesForLanguage.lang.name}'" when {

        "there are no Late Submission or Late Payment penalties" should {

          lazy val html = indexView(Seq(), Seq(), isAgent = false)
          lazy val document = Jsoup.parse(html.toString())

          "render the Late Submission tab with the 'no penalties' message" in {
            document.select(Selectors.lspTab).select("p").text() shouldBe messagesForLanguage.noLSP
          }

          "render the Late Payment tab with the 'no penalties' message" in {
            document.select(Selectors.lppTab).select("p").text() shouldBe messagesForLanguage.noLPP
          }
        }
      }
    }
  }
}
