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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import fixtures.LSPDetailsTestData
import fixtures.messages.LSPCardMessages
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Tag, Text}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LateSubmissionPenaltySummaryCard
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.mocks.MockLSPSummaryListRowHelper

import java.time.LocalDate

class LSPCardHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with DateFormatter
  with LSPDetailsTestData with MockLSPSummaryListRowHelper with TagHelper with SummaryListRowHelper
  with BeforeAndAfterEach with MockFactory {
  this: TestSuite =>

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val tm: TimeMachine = mock[TimeMachine]
  lazy val lspSummaryListRowHelper: LSPCardHelper = new LSPCardHelper(mockLSPSummaryListRowHelper)

  "LSPCardHelper" when {

    for (isBreathingSpace <- Seq(true, false)) {

    Seq(LSPCardMessages.English, LSPCardMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}' and breathingSpace = $isBreathingSpace" when {

        "calling .createLateSubmissionPenaltyCards()" when {

          "rendering a Single Added Point Card" when {

            "Threshold has not been met, and appeal has not been Upheld" should {

              "construct a card with correct messages including the expiry date for the point" in {

                val penalty1 = sampleLateSubmissionPoint.copy(penaltyOrder = Some("1"))

                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
                mockPointExpiryDate(penalty1)(testPointExpiryRow)
                mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(None)
                if (isBreathingSpace) mockBreathingSpaceStatusRow()(testBreathingSpaceRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleLateSubmissionPoint), 2, 1, false) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testPointExpiryRow
                    ),
                    cardTitle = s"${messagesForLanguage.cardTitlePoint(1)}: ${messagesForLanguage.lateUpdate}",
                    status = getTagStatus(penalty1, false, 2),
                    penaltyPoint = "1",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }

            "Threshold has not been met but an appeal has been Upheld" should {

              "construct a card with correct messages without the expiry date for the point" in {

                val penalty = sampleLateSubmissionPoint.copy(
                  appealInformation = Some(Seq(AppealInformationType(
                    appealStatus = Some(AppealStatusEnum.Upheld),
                    appealLevel = Some(AppealLevelEnum.FirstStageAppeal)
                  )))
                )
                val penalty1 = penalty.copy(penaltyOrder = Some("1"))
                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
                mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(None)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(penalty), 2, 1, false) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = s"${messagesForLanguage.cardTitlePoint(1)}: ${messagesForLanguage.lateUpdate}",
                    status = getTagStatus(penalty1, false, 2),
                    penaltyPoint = "1",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    isAppealedPoint = true,
                    appealStatus = Some(AppealStatusEnum.Upheld),
                    appealLevel = Some(AppealLevelEnum.FirstStageAppeal),
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }

            "Threshold has been met" should {

              "construct a card with correct messages without the expiry date for the point" in {

                val penalty1 = sampleLateSubmissionPoint.copy(penaltyOrder = Some("1"))

                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
                mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(None)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleLateSubmissionPoint), 1, 1, false) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = s"${messagesForLanguage.cardTitlePoint(1)}: ${messagesForLanguage.lateUpdate}",
                    status = getTagStatus(penalty1, false, 1),
                    penaltyPoint = "1",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }

            "The point is an adjustment point FAP" when {

              "threshold has been met" should {

                "construct an adjustment card for an added point without an expiry date" in {

                  val adjustedPointAddedPenalty = sampleRemovedPenaltyPoint.copy(
                    penaltyOrder = Some("01"),
                    penaltyStatus = LSPPenaltyStatusEnum.Active
                  )
                  val penalty1 = adjustedPointAddedPenalty.copy(penaltyOrder = Some("1"))

                  lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(adjustedPointAddedPenalty), 1, 1, false) shouldBe
                    Seq(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq(
                        summaryListRow(
                          messagesForLanguage.addedOnKey,
                          Html(dateToString(penalty1.penaltyCreationDate))
                        ),
                      ),
                      cardTitle = messagesForLanguage.cardTitleAdjustmentPoint(1),
                      status = getTagStatus(penalty1, false, 1),
                      penaltyPoint = "1",
                      penaltyId = penalty1.penaltyNumber,
                      isReturnSubmitted = true,
                      isAddedPoint = true,
                      isAddedOrRemovedPoint = true,
                      penaltyCategory = penalty1.penaltyCategory,
                      dueDate = penalty1.dueDate.map(dateToString(_))
                    ))
                }
              }

              "threshold has NOT been met" should {

                "construct an adjustment card for an added point with an expiry date" in {

                  val adjustedPointAddedPenalty = sampleRemovedPenaltyPoint.copy(
                    penaltyOrder = Some("01"),
                    penaltyStatus = LSPPenaltyStatusEnum.Active
                  )
                  val penalty1 = adjustedPointAddedPenalty.copy(penaltyOrder = Some("1"))

                  mockPointExpiryDate(penalty1)(testPointExpiryRow)

                  lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(adjustedPointAddedPenalty), 2, 1, false) shouldBe
                    Seq(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq(
                        summaryListRow(
                          messagesForLanguage.addedOnKey,
                          Html(dateToString(penalty1.penaltyCreationDate))
                        ),
                        testPointExpiryRow
                      ),
                      cardTitle = messagesForLanguage.cardTitleAdjustmentPoint(1),
                      status = getTagStatus(penalty1, false, 2),
                      penaltyPoint = "1",
                      penaltyId = penalty1.penaltyNumber,
                      isReturnSubmitted = true,
                      isAddedPoint = true,
                      isAddedOrRemovedPoint = true,
                      penaltyCategory = penalty1.penaltyCategory,
                      dueDate = penalty1.dueDate.map(dateToString(_))
                    ))
                }
              }
            }
          }

          "rendering a Single Financial Penalty Card" should {

            "construct a card with correct messages including penalty amount" in {

              val penalty1 = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("1"))

              mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
              mockPayPenaltyByRow(penalty1, 1)(None)
              mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
              mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
              mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
              mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
              mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(Some(testAppealStatusRow))
              (tm.getCurrentDate _).expects().returning(LocalDate.of(2021, 3, 6)).twice()


              lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleLateSubmissionPenaltyCharge), 1, 1, false) shouldBe
                Seq(LateSubmissionPenaltySummaryCard(
                  cardRows = Seq(
                    testTaxPeriodRow,
                    testTaxYearRow,
                    testDueDateRow,
                    testReceivedDateRow,
                    testAppealStatusRow
                  ),
                  cardTitle = messagesForLanguage.cardTitleFinancialPoint(1, s": ${messagesForLanguage.lateUpdate}", "200"),
                  status = getTagStatus(penalty1, false, 1),
                  penaltyPoint = "1",
                  penaltyId = penalty1.penaltyNumber,
                  isReturnSubmitted = true,
                  penaltyCategory = penalty1.penaltyCategory,
                  dueDate = penalty1.dueDate.map(dateToString(_))
                ))
            }
          }


          "rendering a multiple Financial Penalty Cards so that the threshold is breached leading to additional penalty amount" should {

            "construct cards with correct messages including penalty amount" in {

              val penalty1 = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("1"))
              val penalty2 = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("2"))
              val penalty3 = sampleLateSubmissionPenaltyCharge.copy(penaltyOrder = Some("3"))

              Seq(penalty1, penalty2, penalty3).foreach { penalty =>
                mockMissingOrLateIncomeSourcesSummaryRow(penalty)(None)
                mockPayPenaltyByRow(penalty, 2)(Some(testPayPenaltyByRow))
                mockTaxPeriodSummaryRow(penalty)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty)(testReceivedDateRow)
                mockAppealStatusSummaryRow(penalty.appealStatus, penalty.appealLevel)(Some(testAppealStatusRow))
                (tm.getCurrentDate _).expects().returning(LocalDate.of(2021, 3, 6)).twice()
              }

              lspSummaryListRowHelper.createLateSubmissionPenaltyCards(
                penalties = Seq(penalty3, penalty2, penalty1),
                threshold = 2,
                activePoints = 3,
                isBreathingSpace = false
              ) shouldBe
                Seq(
                  LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testPayPenaltyByRow,
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleAdditionalFinancialPoint("200", s": ${messagesForLanguage.lateUpdate}"),
                    status = getTagStatus(penalty3, false, 2),
                    penaltyPoint = "3",
                    penaltyId = penalty3.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty3.penaltyCategory,
                    dueDate = penalty3.dueDate.map(dateToString(_))
                  ),
                  LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testPayPenaltyByRow,
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleFinancialPoint(2, s": ${messagesForLanguage.lateUpdate}", "200"),
                    status = getTagStatus(penalty2, false, 2),
                    penaltyPoint = "2",
                    penaltyId = penalty2.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty2.penaltyCategory,
                    dueDate = penalty2.dueDate.map(dateToString(_))
                  ),
                  LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testPayPenaltyByRow,
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleFinancialPointNoThreshold(1, s": ${messagesForLanguage.lateUpdate}"),
                    status = getTagStatus(penalty1, false, 2),
                    penaltyPoint = "1",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  )
                )
            }
          }

          "rendering a Single Removed Point" when {

            "the penalty has Expired" should {
              "construct a card with correct messages including the point expired on" in {
                val penalty1 = sampleExpiredPenaltyPoint
                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
                mockPointExpiredOnRow(penalty1)(testPointExpiredOnRow)
                mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(Some(testAppealStatusRow))

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(penalty1), 2, 1, false) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testPointExpiredOnRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                    status = getTagStatus(penalty1, false, 2),
                    penaltyPoint = "",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    isAddedOrRemovedPoint = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    isManuallyRemovedPoint = true,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }

            "the period of compliance has been achieved" should {
              "construct a card with correct messages" in {
                val penalty1 = sampleRemovedPenaltyPointWithPocAchieved
                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(penalty1), 2, 1, false, pointsRemovedAfterPeriodOfCompliance = true) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                    status = getTagStatus(penalty1, false, 2, pointsRemovedAfterPoc = Some(true)),
                    penaltyPoint = "",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    isAddedOrRemovedPoint = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    isManuallyRemovedPoint = true,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }

            "Adjustment is a FAP" should {

              "construct a card with correct messages" in {

                val penalty1 = sampleRemovedPenaltyPoint

                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(penalty1), 2, 1, false) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                    status = getTagStatus(penalty1, false, 2),
                    penaltyPoint = "",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    isAddedOrRemovedPoint = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }

            "Point has been manually removed" should {

              "construct a card with correct messages" in {

                val penalty1 = sampleRemovedPenaltyPoint

                mockMissingOrLateIncomeSourcesSummaryRow(penalty1)(None)
                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockTaxYearSummaryRow(penalty1)(Some(testTaxYearRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(penalty1), 2, 1, false) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testTaxYearRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                    status = getTagStatus(penalty1, false, 2),
                    penaltyPoint = "",
                    penaltyId = penalty1.penaltyNumber,
                    isReturnSubmitted = true,
                    isAddedOrRemovedPoint = true,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = penalty1.dueDate.map(dateToString(_))
                  ))
              }
            }
          }

          "Status Tag" should {
            "become 'Overdue' when chargeDueDate is before today" in {
              implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
              (tm.getCurrentDate _).expects().returning(LocalDate.of(2025, 8, 11))

              val penalty = sampleLateSubmissionPenaltyCharge.copy(
                penaltyOrder = Some("1"),
                chargeDueDate = Some(LocalDate.of(2025, 8, 10))
              )

              mockMissingOrLateIncomeSourcesSummaryRow(penalty)(None)
              mockPayPenaltyByRow(penalty, threshold = 1)(None)
              mockTaxPeriodSummaryRow(penalty)(Some(testTaxPeriodRow))
              mockTaxYearSummaryRow(penalty)(Some(testTaxYearRow))
              mockDueDateSummaryRow(penalty)(Some(testDueDateRow))
              mockReceivedDateSummaryRow(penalty)(testReceivedDateRow)
              mockAppealStatusSummaryRow(penalty.appealStatus, penalty.appealLevel)(None)

              val cards = lspSummaryListRowHelper.createLateSubmissionPenaltyCards(
                Seq(penalty),
                threshold = 1,
                activePoints = 1,
                isBreathingSpace = false
              )(messages)

              cards.head.status shouldBe Tag(Text(messages("status.overdue")), "govuk-tag--red")
            }
            "become 'due' when chargeDueDate is after today" in {
              implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
              (tm.getCurrentDate _).expects().returning(LocalDate.of(2025, 8, 11))

              val penalty = sampleLateSubmissionPenaltyCharge.copy(
                penaltyOrder = Some("1"),
                chargeDueDate = Some(LocalDate.of(2025, 8, 12))
              )

              mockMissingOrLateIncomeSourcesSummaryRow(penalty)(None)
              mockPayPenaltyByRow(penalty, threshold = 1)(None)
              mockTaxPeriodSummaryRow(penalty)(Some(testTaxPeriodRow))
              mockTaxYearSummaryRow(penalty)(Some(testTaxYearRow))
              mockDueDateSummaryRow(penalty)(Some(testDueDateRow))
              mockReceivedDateSummaryRow(penalty)(testReceivedDateRow)
              mockAppealStatusSummaryRow(penalty.appealStatus, penalty.appealLevel)(None)

              val cards = lspSummaryListRowHelper.createLateSubmissionPenaltyCards(
                Seq(penalty),
                threshold = 1,
                activePoints = 1,
                isBreathingSpace = false
              )(messages)

              cards.head.status shouldBe Tag(Text(messages("status.due")), "govuk-tag--red")
            }
          }
        }
      }
      }
    }
  }
}
