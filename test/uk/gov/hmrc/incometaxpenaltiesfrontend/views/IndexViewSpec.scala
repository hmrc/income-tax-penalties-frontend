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

import fixtures.PenaltiesDetailsTestData
import fixtures.messages.{IndexViewMessages, LSPOverviewMessages}
import fixtures.views.BaseSelectors
import org.jsoup.nodes.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.*
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.IndexView

import java.time.LocalDate

class IndexViewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with ScalaFutures with PenaltiesDetailsTestData with ViewBehaviours {

  lazy val indexView: IndexView = app.injector.instanceOf[IndexView]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  private val somePocDate: Option[LocalDate] = Some(LocalDate.of(2028, 4, 1))

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

    Seq(
      IndexViewMessages.English -> LSPOverviewMessages.English,
      IndexViewMessages.Welsh -> LSPOverviewMessages.Welsh
    ).foreach { case (messagesForLanguage, lspMessages) =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"rendering in the language '${messagesForLanguage.lang.name}'" when {

        Seq(true, false).foreach { isAgent =>

          s"being viewed by an ${if (isAgent) "agent" else "individual"}" should {

            "there are no Late Submission or Late Payment penalties" should {

              lazy val html = indexView(
                lspOverviewData = None,
                lspCardData = Seq(),
                lppCardData = Seq(),
                penaltiesOverviewViewModel = PenaltiesOverviewViewModel(Seq(), hasFinancialCharge = false),
                isAgent = isAgent,
                actionsToRemoveLinkDate = somePocDate
              )
              implicit lazy val document: Document = asDocument(html)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.lspTab -> (if (isAgent) messagesForLanguage.noLSPAgent else messagesForLanguage.noLSP),
                Selectors.lppTab -> (if
                (isAgent) messagesForLanguage.noLPPAgent else messagesForLanguage.noLPPIndividual)
              )
            }

            "there is a Late Submission Point (no financial amount)" should {

              lazy val html = indexView(
                lspOverviewData = Some(LSPOverviewViewModel(lateSubmissionPenalty)),
                lspCardData = Seq(),
                lppCardData = Seq(),
                penaltiesOverviewViewModel = PenaltiesOverviewViewModel(Seq(LSPPointsActive(1)), hasFinancialCharge = false),
                isAgent = isAgent,
                actionsToRemoveLinkDate = somePocDate
              )

              implicit lazy val document: Document = asDocument(html)

              "render the correct content for the Overview section (no check and pay button)" which {

                behave like pageWithExpectedElementsAndMessages(
                  Selectors.overviewH2 -> messagesForLanguage.overviewH2,
                  Selectors.overviewP1 -> messagesForLanguage.overviewLSPPointsNoBullets(1, isAgent),
                  concat(Selectors.lspTab, Selectors.p(1)) -> lspMessages.pointsTotal(1),
                  concat(Selectors.lspTab, Selectors.p(2)) -> lspMessages.pointsAccruingP1(isAgent)(1),
                  concat(Selectors.lspTab, Selectors.p(3)) -> lspMessages.pointsAccruingP2(isAgent),
                  concat(Selectors.lspTab, Selectors.p(4)) -> lspMessages.pointsAccruingP3(isAgent)(4),
                  concat(Selectors.lspTab, Selectors.link(1)) -> lspMessages.pointsGuidanceLink
                )

                behave like pageWithoutElementsRendered(
                  Selectors.overviewButton
                )
              }
            }

            "there are multiple Late Submission Points, including financial" should {

              lazy val html = indexView(
                lspOverviewData = Some(LSPOverviewViewModel(lateSubmissionPenalty.copy(
                  summary = lateSubmissionPenalty.summary.copy(
                    activePenaltyPoints = 4
                  ),
                  details = Seq(sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPenaltyCharge)
                ))),
                lspCardData = Seq(),
                lppCardData = Seq(),
                penaltiesOverviewViewModel = PenaltiesOverviewViewModel(Seq(
                  LSPNotPaidOrAppealed(1),
                  LSPMaxItem
                ), hasFinancialCharge = true),
                isAgent = isAgent,
                actionsToRemoveLinkDate = somePocDate
              )
              implicit lazy val document: Document = asDocument(html)

              "render the correct content for the Overview section" which {

                behave like pageWithExpectedElementsAndMessages(
                  Selectors.overviewH2 -> messagesForLanguage.overviewH2,
                  Selectors.overviewP1 -> messagesForLanguage.overviewP1(isAgent),
                  Selectors.overviewBullet(1) -> messagesForLanguage.overviewLSPFinancial(1),
                  Selectors.overviewBullet(2) -> messagesForLanguage.overviewLSPPointsMax,
                  Selectors.overviewButton -> messagesForLanguage.overviewCheckAndPay(isAgent),
                  concat(Selectors.lspTab, Selectors.p(1)) -> lspMessages.pointsTotal(4),
                  concat(Selectors.lspTab, Selectors.warning) -> lspMessages.penaltyWarning(isAgent),
                  concat(Selectors.lspTab, Selectors.p(2)) -> lspMessages.penaltyP1(isAgent),
                  concat(Selectors.lspTab, Selectors.link(1)) -> lspMessages.actionsLink(isAgent)
                )
              }
            }
          }
        }
      }
    }
  }
}