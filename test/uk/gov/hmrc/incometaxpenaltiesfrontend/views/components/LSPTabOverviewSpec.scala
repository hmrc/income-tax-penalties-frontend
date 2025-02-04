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

import fixtures.PenaltiesDetailsTestData
import fixtures.messages.LSPOverviewMessages
import fixtures.views.BaseSelectors
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LSPOverviewViewModel
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.components.LSPTabOverview

class LSPTabOverviewSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with PenaltiesDetailsTestData with ViewBehaviours {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lspTabOverview: LSPTabOverview = app.injector.instanceOf[LSPTabOverview]

  object Selectors extends BaseSelectors

  "LSPTabOverview" when {

    Seq(true, false).foreach { isAgent =>

      s"the user type is an ${if(isAgent) "agent" else "individual"}" when {

        Seq(LSPOverviewMessages.English, LSPOverviewMessages.Welsh).foreach { messagesForLanguage =>

          implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

          s"rendering in language '${messagesForLanguage.lang.name}'" when {

            "a single LSP point exists" should {

              val data = LSPOverviewViewModel(lateSubmissionPenalty)
              val lspTabOverviewHtml = lspTabOverview(data, isAgent)

              implicit val document: Document = asDocument(lspTabOverviewHtml)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.p(1) -> messagesForLanguage.pointsTotal(data.pointsTotal),
                Selectors.p(2) -> messagesForLanguage.pointsAccruingP1(isAgent)(data.activePoints),
                Selectors.p(3) -> messagesForLanguage.pointsAccruingP2(isAgent),
                Selectors.p(4) -> messagesForLanguage.pointsAccruingP3(isAgent)(data.threshold),
                Selectors.link(1) -> messagesForLanguage.pointsGuidanceLink
              )

              behave like pageWithoutElementsRendered(
                Selectors.warning
              )
            }

            "2 LSP points exist, and there is more than 1 point left before the threshold is hit" should {

              val data = LSPOverviewViewModel(lateSubmissionPenalty.copy(
                summary = lateSubmissionPenalty.summary.copy(
                  activePenaltyPoints = 2
                ),
                details = Seq(sampleLateSubmissionPoint, sampleLateSubmissionPoint)
              ))
              val lspTabOverviewHtml = lspTabOverview(data, isAgent)

              implicit val document: Document = asDocument(lspTabOverviewHtml)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.p(1) -> messagesForLanguage.pointsTotal(data.pointsTotal),
                Selectors.p(2) -> messagesForLanguage.pointsAccruingP1(isAgent)(data.activePoints),
                Selectors.p(3) -> messagesForLanguage.pointsAccruingP2(isAgent),
                Selectors.p(4) -> messagesForLanguage.pointsAccruingP3(isAgent)(data.threshold),
                Selectors.link(1) -> messagesForLanguage.pointsGuidanceLink
              )

              behave like pageWithoutElementsRendered(
                Selectors.warning
              )
            }

            "3 LSP points exist, and there is only 1 more point left before the threshold is hit" should {

              val data = LSPOverviewViewModel(lateSubmissionPenalty.copy(
                summary = lateSubmissionPenalty.summary.copy(
                  activePenaltyPoints = 3
                ),
                details = Seq(sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPoint)
              ))
              val lspTabOverviewHtml = lspTabOverview(data, isAgent)

              implicit val document: Document = asDocument(lspTabOverviewHtml)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.p(1) -> messagesForLanguage.pointsTotal(data.pointsTotal),
                Selectors.warning -> messagesForLanguage.pointsAccruingWarning(isAgent),
                Selectors.p(2) -> messagesForLanguage.pointsAccruingP1(isAgent)(data.activePoints),
                Selectors.p(3) -> messagesForLanguage.pointsAccruingP2(isAgent),
                Selectors.p(4) -> messagesForLanguage.pointsAccruingP3(isAgent)(data.threshold),
                Selectors.link(1) -> messagesForLanguage.pointsGuidanceLink
              )
            }

            "4 LSP points exist, penalty threshold reached with one financial charge" should {

              val data = LSPOverviewViewModel(lateSubmissionPenalty.copy(
                summary = lateSubmissionPenalty.summary.copy(
                  activePenaltyPoints = 4
                ),
                details = Seq(sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPenaltyCharge)
              ))
              val lspTabOverviewHtml = lspTabOverview(data, isAgent)

              implicit val document: Document = asDocument(lspTabOverviewHtml)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.p(1) -> messagesForLanguage.pointsTotal(data.pointsTotal),
                Selectors.warning -> messagesForLanguage.penaltyWarning(isAgent),
                Selectors.p(2) -> messagesForLanguage.penaltyP1(isAgent),
                Selectors.link(1) -> messagesForLanguage.actionsLink(isAgent)
              )
            }

            "4 LSP points exist, penalty threshold reached with more than one financial charge" should {

              val data = LSPOverviewViewModel(lateSubmissionPenalty.copy(
                summary = lateSubmissionPenalty.summary.copy(
                  activePenaltyPoints = 4
                ),
                details = Seq(sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPoint, sampleLateSubmissionPenaltyCharge, sampleLateSubmissionPenaltyCharge)
              ))
              val lspTabOverviewHtml = lspTabOverview(data, isAgent)

              implicit val document: Document = asDocument(lspTabOverviewHtml)

              behave like pageWithExpectedElementsAndMessages(
                Selectors.p(1) -> messagesForLanguage.pointsTotal(data.pointsTotal),
                Selectors.warning -> messagesForLanguage.additionalPenaltyWarning(isAgent),
                Selectors.p(2) -> messagesForLanguage.additionalPenaltyP1(isAgent),
                Selectors.link(1) -> messagesForLanguage.actionsLink(isAgent)
              )
            }
          }
        }
      }
    }
  }
}
