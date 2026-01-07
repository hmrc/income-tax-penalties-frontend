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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.components

import fixtures.messages.IndexViewMessages
import fixtures.views.BaseSelectors
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.*
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.components.PenaltiesOverview

class PenaltiesOverviewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ViewBehaviours {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val penaltiesOverview: PenaltiesOverview = app.injector.instanceOf[PenaltiesOverview]

  object Selectors extends BaseSelectors {
    val penaltiesOverview = "#penaltiesOverview"
    val overviewH2: String = s"$penaltiesOverview ${h2(1)}"
    val overviewP1: String = s"$penaltiesOverview ${p(1)}"
    val overviewBullet: Int => String = i => s"$penaltiesOverview ${bullet(i)}"
    val overviewButton: String = s"$penaltiesOverview $button"
  }

  "PenaltiesOverview" when {

    Seq(true, false).foreach { isAgent =>

      s"the user type is an ${if (isAgent) "agent" else "individual"}" when {

        Seq(IndexViewMessages.English, IndexViewMessages.Welsh).foreach { messagesForLanguage =>

          implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

          s"rendering in language '${messagesForLanguage.lang.name}'" when {

            "a single piece of overview content exists AND no financial charge either" should {

              "output a paragraph without bullet points AND no check and pay button" which {

                val penaltiesOverviewHtml = penaltiesOverview(
                  PenaltiesOverviewViewModel(Seq(LSPPointsActive(1)), hasFinancialCharge = false),
                  isAgent
                )

                implicit val document: Document = asDocument(penaltiesOverviewHtml)

                behave like pageWithExpectedElementsAndMessages(
                  Selectors.overviewH2 -> messagesForLanguage.overviewH2,
                  Selectors.overviewP1 -> messagesForLanguage.overviewLSPPointsNoBullets(1, isAgent)
                )

                behave like pageWithoutElementsRendered(
                  Selectors.overviewButton
                )
              }
            }

            "multiple pieces of overview content exist AND a financial charge" should {

              "output a paragraph followed by bullet points AND a check and pay button" which {

                val penaltiesOverviewHtml = penaltiesOverview(
                  PenaltiesOverviewViewModel(
                    Seq(
                      LSPPointsActive(1),
                      LPPNotPaidOrAppealed(1)
                    ),
                    hasFinancialCharge = true
                  ),
                  isAgent
                )

                implicit val document: Document = asDocument(penaltiesOverviewHtml)

                behave like pageWithExpectedElementsAndMessages(
                  Selectors.overviewH2 -> messagesForLanguage.overviewH2,
                  Selectors.overviewP1 -> messagesForLanguage.overviewP1(isAgent),
                  Selectors.overviewBullet(1) -> messagesForLanguage.overviewLSPPoints(1),
                  Selectors.overviewBullet(2) -> messagesForLanguage.overviewLPP(1),
                  Selectors.overviewButton -> messagesForLanguage.overviewCheckAndPay(isAgent)
                )

                "has a link to the check and pay page" in {
                  val expectedPath: String =
                    if (isAgent) "/report-quarterly/income-and-expenses/view/agents/what-your-client-owes"
                    else "/report-quarterly/income-and-expenses/view/what-you-owe"

                  document.select(Selectors.overviewButton).attr("href") shouldBe appConfig.viewAndChangeBaseUrl + expectedPath
                }
              }
            }
          }
        }
      }
    }
  }
}