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

import fixtures.LSPDetailsTestData
import fixtures.messages.{LSPCardMessages, PenaltyTagStatusMessages}
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LateSubmissionPenaltySummaryCard
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.TagHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.components.SummaryCardLSP

class SummaryCardLSPSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite
  with LSPDetailsTestData with TagHelper with DateFormatter {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  lazy val summaryCard: SummaryCardLSP = app.injector.instanceOf[SummaryCardLSP]

  "SummaryCardLSP" when {

    Seq(
      LSPCardMessages.English -> PenaltyTagStatusMessages.English,
      LSPCardMessages.Welsh -> PenaltyTagStatusMessages.Welsh
    ).foreach { case (messagesForLanguage, penaltyStatusMessages) =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))
      implicit val tm: TimeMachine = timeMachine

      s"rendering in language '${messagesForLanguage.lang.name}'" when {

        List(true, false).foreach { isAgent =>

          s"calling .apply(isAgent = $isAgent)" when {

            "provided with a Late Submission Penalty" when {

              "penalty is NOT appealed" when {

                "it's not an added or remove point" when {

                  "when the return has been received late" should {

                    "generate a Summary Card with correct content including an Appeal Link" in {

                      val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                        cardRows = Seq.empty,
                        cardTitle = messagesForLanguage.cardTitlePoint(1),
                        status = getTagStatus(sampleLateSubmissionPoint),
                        penaltyPoint = "1",
                        penaltyId = sampleLateSubmissionPoint.penaltyNumber,
                        isReturnSubmitted = true,
                        penaltyCategory = sampleLateSubmissionPoint.penaltyCategory,
                        dueDate = sampleLateSubmissionPoint.dueDate.map(dateToString(_))
                      ), isAgent)

                      val document = Jsoup.parse(summaryCardHtml.toString)

                      document.select("h3").text() shouldBe messagesForLanguage.cardTitlePoint(1)
                      document.select(s"#penalty-id-${sampleLateSubmissionPoint.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active

                      val appealLink = document.select(s"#penalty-id-${sampleLateSubmissionPoint.penaltyNumber}-appealLink")
                      appealLink.text() shouldBe messagesForLanguage.cardLinksAppeal
                      appealLink.attr("href") shouldBe controllers.routes.AppealsController.redirectToAppeals(
                        sampleLateSubmissionPoint.penaltyNumber, isAgent
                      ).url
                    }
                  }

                  "when the return has NOT been received" should {

                    "generate a Summary Card with correct content including an Check if you can appeal this penalty" in {

                      val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                        cardRows = Seq.empty,
                        cardTitle = messagesForLanguage.cardTitlePoint(1),
                        status = getTagStatus(sampleLateSubmissionPoint),
                        penaltyPoint = "1",
                        penaltyId = sampleRemovedPenaltyPoint.penaltyNumber,
                        isReturnSubmitted = false,
                        penaltyCategory = sampleRemovedPenaltyPoint.penaltyCategory,
                        dueDate = samplePenaltyPointNotSubmitted.dueDate.map(dateToString(_))
                      ), isAgent)

                      val document = Jsoup.parse(summaryCardHtml.toString)

                      document.select("h3").text() shouldBe messagesForLanguage.cardTitlePoint(1)
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active

                      val appealLink = document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-appealLink")
                      appealLink.text() shouldBe messagesForLanguage.cardLinksFindOutHowToAppeal
                      appealLink.attr("href") shouldBe controllers.routes.AppealsController.redirectToAppeals(
                        sampleRemovedPenaltyPoint.penaltyNumber, isAgent
                      ).url
                    }
                  }
                }

                "it's adjustment point" when {

                  "when the point has been added" should {

                    "generate a Summary Card with correct content including a cannot appeal message" in {

                      val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                        cardRows = Seq.empty,
                        cardTitle = messagesForLanguage.cardTitleAdjustmentPoint(1),
                        status = getTagStatus(sampleLateSubmissionPoint),
                        penaltyPoint = "1",
                        penaltyId = sampleRemovedPenaltyPoint.penaltyNumber,
                        isReturnSubmitted = true,
                        penaltyCategory = sampleRemovedPenaltyPoint.penaltyCategory,
                        dueDate = samplePenaltyPointNotSubmitted.dueDate.map(dateToString(_)),
                        isAddedPoint = true,
                        isAddedOrRemovedPoint = true
                      ), isAgent)

                      val document = Jsoup.parse(summaryCardHtml.toString)

                      document.select("h3").text() shouldBe messagesForLanguage.cardTitleAdjustmentPoint(1)
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-cannotAppeal").text() shouldBe messagesForLanguage.cardLinksAdjustedPointCannotAppeal
                    }
                  }

                  "when the point has been removed" should {

                    "generate a Summary Card with correct content (no link or content in the card footer)" in {

                      val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                        cardRows = Seq.empty,
                        cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                        status = getTagStatus(sampleLateSubmissionPoint),
                        penaltyPoint = "",
                        penaltyId = sampleRemovedPenaltyPoint.penaltyNumber,
                        isReturnSubmitted = true,
                        penaltyCategory = sampleRemovedPenaltyPoint.penaltyCategory,
                        dueDate = samplePenaltyPointNotSubmitted.dueDate.map(dateToString(_)),
                        isAddedOrRemovedPoint = true
                      ), isAgent)

                      val document = Jsoup.parse(summaryCardHtml.toString)

                      document.select("h3").text() shouldBe messagesForLanguage.cardTitleRemovedPoint
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-cannotAppeal").isEmpty shouldBe true
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-findOutHowAppeal").isEmpty shouldBe true
                      document.select(s"#penalty-id-${sampleRemovedPenaltyPoint.penaltyNumber}-appealLink").isEmpty shouldBe true
                    }
                  }
                }
              }

              "penalty has been appealed" when {

                "appeal was rejected at 1st Stage" should {

                  "generate a Summary Card with correct content including a Review Appeal link" in {

                    val appealedPenalty = samplePenaltyPointAppeal(AppealStatusEnum.Rejected, AppealLevelEnum.FirstStageAppeal)
                    val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq.empty,
                      cardTitle = messagesForLanguage.cardTitlePoint(1),
                      status = getTagStatus(appealedPenalty),
                      penaltyPoint = "",
                      penaltyId = appealedPenalty.penaltyNumber,
                      isReturnSubmitted = true,
                      penaltyCategory = appealedPenalty.penaltyCategory,
                      dueDate = appealedPenalty.dueDate.map(dateToString(_)),
                      isAppealedPoint = true,
                      appealStatus = Some(AppealStatusEnum.Rejected),
                      appealLevel = Some(AppealLevelEnum.FirstStageAppeal)
                    ), isAgent)

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h3").text() shouldBe messagesForLanguage.cardTitlePoint(1)
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-cannotAppeal").isEmpty shouldBe true
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-findOutHowAppeal").isEmpty shouldBe true

                    val appealLink = document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-appealLink")
                    appealLink.text() shouldBe messagesForLanguage.cardLinksReviewAppeal
                    appealLink.attr("href") shouldBe controllers.routes.AppealsController.redirectToAppeals(
                      sampleRemovedPenaltyPoint.penaltyNumber,
                      isAgent,
                      is2ndStageAppeal = true
                    ).url
                  }
                }

                "appeal was rejected at 2nd Stage" should {

                  "generate a Summary Card with correct content (no link or content in the card footer)" in {

                    val appealedPenalty = samplePenaltyPointAppeal(AppealStatusEnum.Rejected, AppealLevelEnum.SecondStageAppeal)
                    val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq.empty,
                      cardTitle = messagesForLanguage.cardTitlePoint(1),
                      status = getTagStatus(appealedPenalty),
                      penaltyPoint = "",
                      penaltyId = appealedPenalty.penaltyNumber,
                      isReturnSubmitted = true,
                      penaltyCategory = appealedPenalty.penaltyCategory,
                      dueDate = appealedPenalty.dueDate.map(dateToString(_)),
                      isAppealedPoint = true,
                      appealStatus = Some(AppealStatusEnum.Rejected),
                      appealLevel = Some(AppealLevelEnum.SecondStageAppeal)
                    ), isAgent)

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h3").text() shouldBe messagesForLanguage.cardTitlePoint(1)
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-cannotAppeal").isEmpty shouldBe true
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-findOutHowAppeal").isEmpty shouldBe true
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-appealLink").isEmpty shouldBe true
                  }
                }

                "active appeal is in progress" should {

                  "generate a Summary Card with correct content (no link or content in the card footer)" in {

                    val appealedPenalty = samplePenaltyPointAppeal(AppealStatusEnum.Under_Appeal, AppealLevelEnum.FirstStageAppeal)
                    val summaryCardHtml = summaryCard(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq.empty,
                      cardTitle = messagesForLanguage.cardTitlePoint(1),
                      status = getTagStatus(appealedPenalty),
                      penaltyPoint = "",
                      penaltyId = appealedPenalty.penaltyNumber,
                      isReturnSubmitted = true,
                      penaltyCategory = appealedPenalty.penaltyCategory,
                      dueDate = appealedPenalty.dueDate.map(dateToString(_)),
                      isAppealedPoint = true,
                      appealStatus = Some(AppealStatusEnum.Rejected),
                      appealLevel = Some(AppealLevelEnum.SecondStageAppeal)
                    ), isAgent)

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h3").text() shouldBe messagesForLanguage.cardTitlePoint(1)
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-status").text() shouldBe penaltyStatusMessages.active
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-cannotAppeal").isEmpty shouldBe true
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-findOutHowAppeal").isEmpty shouldBe true
                    document.select(s"#penalty-id-${appealedPenalty.penaltyNumber}-appealLink").isEmpty shouldBe true
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
