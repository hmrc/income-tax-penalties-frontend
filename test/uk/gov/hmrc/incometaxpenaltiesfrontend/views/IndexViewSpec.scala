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
import fixtures.views.BaseSelectors
import org.jsoup.nodes.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.PenaltiesOverviewViewModel
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndexView

class IndexViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures with ViewBehaviours {

  lazy val indexView: IndexView = app.injector.instanceOf[IndexView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  object Selectors extends BaseSelectors {
    val lspTab = "#lspTab"
    val lppTab = "#lppTab"
    val penaltiesOverview = "#penaltiesOverview"
    val overviewH2: String = s"$penaltiesOverview ${h2(1)}"
    val overviewP1: String = s"$penaltiesOverview ${p(1)}"
    val overviewBullet: Int => String = i => s"$penaltiesOverview ${bullet(i)}"
    val overviewButton: String = s"$penaltiesOverview $button"
  }

  "indexView" when {

    Seq(IndexViewMessages.English, IndexViewMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"rendering in the language '${messagesForLanguage.lang.name}'" when {

        Seq(true, false).foreach { isAgent =>

          s"being viewed by an ${if (isAgent) "agent" else "individual"}" should {

            "there are no Late Submission or Late Payment penalties" should {

              lazy val html = indexView(Seq(), Seq(), PenaltiesOverviewViewModel(Seq(), hasFinancialCharge = false), isAgent = isAgent)
              implicit lazy val document: Document = asDocument(html)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.lspTab -> messagesForLanguage.noLSP,
                Selectors.lppTab -> messagesForLanguage.noLPP
              )
            }

            "there is a Late Submission Point (no financial amount)" should {

              lazy val html = indexView(
                Seq(),
                Seq(),
                PenaltiesOverviewViewModel(Seq(messagesForLanguage.overviewLSPPoints(1)), hasFinancialCharge = false),
                isAgent = isAgent
              )
              implicit lazy val document: Document = asDocument(html)

              "render the correct content for the Overview section (no check and pay button)" which {

                behave like pageWithExpectedElementsAndMessages(
                  Selectors.overviewH2 -> messagesForLanguage.overviewH2,
                  Selectors.overviewP1 -> messagesForLanguage.overviewP1NoBullets(isAgent)(messagesForLanguage.overviewLSPPoints(1))
                )

                behave like pageWithoutElementsRendered(
                  Selectors.overviewButton
                )
              }
            }

            "there are multiple Late Submission Point, including financial" should {

              lazy val html = indexView(
                Seq(),
                Seq(),
                PenaltiesOverviewViewModel(Seq(
                  messagesForLanguage.overviewLSPFinancial(1),
                  messagesForLanguage.overviewLSPPointsMax
                ), hasFinancialCharge = true),
                isAgent = isAgent
              )
              implicit lazy val document: Document = asDocument(html)

              "render the correct content for the Overview section" which {

                behave like pageWithExpectedElementsAndMessages(
                  Selectors.overviewH2 -> messagesForLanguage.overviewH2,
                  Selectors.overviewP1 -> messagesForLanguage.overviewP1(isAgent),
                  Selectors.overviewBullet(1) -> messagesForLanguage.overviewLSPFinancial(1),
                  Selectors.overviewBullet(2) -> messagesForLanguage.overviewLSPPointsMax,
                  Selectors.overviewButton -> messagesForLanguage.overviewButton(isAgent)
                )
              }
            }
          }
        }
      }
    }
  }
}
