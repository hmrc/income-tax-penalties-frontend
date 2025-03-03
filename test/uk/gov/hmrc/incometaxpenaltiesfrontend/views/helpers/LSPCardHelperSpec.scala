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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.LSPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LateSubmissionPenaltySummaryCard
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.mocks.MockLSPSummaryListRowHelper

class LSPCardHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with DateFormatter
  with LSPDetailsTestData with MockLSPSummaryListRowHelper with TagHelper with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lspSummaryListRowHelper: LSPCardHelper = new LSPCardHelper(mockLSPSummaryListRowHelper)

  "LSPCardHelper" when {

    Seq(LSPCardMessages.English, LSPCardMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .createLateSubmissionPenaltyCards()" when {

          "rendering a Single Added Point Card" when {

            "Threshold has not been met, and appeal has not been Upheld" should {

              "construct a card with correct messages including the expiry date for the point" in {

                val penalty1 = sampleLateSubmissionPoint.copy(penaltyOrder = Some("1"))

                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
                mockPointExpiryDate(penalty1)(testPointExpiryRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleLateSubmissionPoint), 2, 1) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testPointExpiryRow
                    ),
                    cardTitle = messagesForLanguage.cardTitlePoint(1),
                    status = getTagStatus(penalty1),
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

                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(penalty), 2, 1) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = messagesForLanguage.cardTitlePoint(1),
                    status = getTagStatus(penalty1),
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

                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleLateSubmissionPoint), 1, 1) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testDueDateRow,
                      testReceivedDateRow
                    ),
                    cardTitle = messagesForLanguage.cardTitlePoint(1),
                    status = getTagStatus(penalty1),
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

                  lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(adjustedPointAddedPenalty), 1, 1) shouldBe
                    Seq(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq(
                        summaryListRow(
                          messagesForLanguage.addedOnKey,
                          Html(dateToString(penalty1.penaltyCreationDate))
                        ),
                      ),
                      cardTitle = messagesForLanguage.cardTitleAdjustmentPoint(1),
                      status = getTagStatus(penalty1),
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

                  lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(adjustedPointAddedPenalty), 2, 1) shouldBe
                    Seq(LateSubmissionPenaltySummaryCard(
                      cardRows = Seq(
                        summaryListRow(
                          messagesForLanguage.addedOnKey,
                          Html(dateToString(penalty1.penaltyCreationDate))
                        ),
                        testPointExpiryRow
                      ),
                      cardTitle = messagesForLanguage.cardTitleAdjustmentPoint(1),
                      status = getTagStatus(penalty1),
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

              mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
              mockDueDateSummaryRow(penalty1)(Some(testDueDateRow))
              mockReceivedDateSummaryRow(penalty1)(testReceivedDateRow)
              mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(Some(testAppealStatusRow))

              lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleLateSubmissionPenaltyCharge), 1, 1) shouldBe
                Seq(LateSubmissionPenaltySummaryCard(
                  cardRows = Seq(
                    testTaxPeriodRow,
                    testDueDateRow,
                    testReceivedDateRow,
                    testAppealStatusRow
                  ),
                  cardTitle = messagesForLanguage.cardTitleFinancialPoint(1, "200"),
                  status = getTagStatus(penalty1),
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
                mockTaxPeriodSummaryRow(penalty)(Some(testTaxPeriodRow))
                mockDueDateSummaryRow(penalty)(Some(testDueDateRow))
                mockReceivedDateSummaryRow(penalty)(testReceivedDateRow)
                mockAppealStatusSummaryRow(penalty.appealStatus, penalty.appealLevel)(Some(testAppealStatusRow))
              }

              lspSummaryListRowHelper.createLateSubmissionPenaltyCards(
                penalties = Seq(penalty3, penalty2, penalty1),
                threshold = 2,
                activePoints = 3
              ) shouldBe
                Seq(
                  LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleAdditionalFinancialPoint(amount = "200"),
                    status = getTagStatus(penalty3),
                    penaltyPoint = "3",
                    penaltyId = penalty3.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty3.penaltyCategory,
                    dueDate = penalty3.dueDate.map(dateToString(_))
                  ),
                  LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleFinancialPoint(point = 2, amount = "200"),
                    status = getTagStatus(penalty2),
                    penaltyPoint = "2",
                    penaltyId = penalty2.penaltyNumber,
                    isReturnSubmitted = true,
                    penaltyCategory = penalty2.penaltyCategory,
                    dueDate = penalty2.dueDate.map(dateToString(_))
                  ),
                  LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testDueDateRow,
                      testReceivedDateRow,
                      testAppealStatusRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleFinancialPoint(point = 1, amount = "200"),
                    status = getTagStatus(penalty1),
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

            "Adjustment is a FAP" should {

              "construct a card with correct messages including the expiry date for the point" in {

                val penalty1 = sampleRemovedPenaltyPoint

                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockExpiryReasonSummaryRow(penalty1)(Some(testExpiryReasonRow))

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleRemovedPenaltyPoint), 2, 1) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testExpiryReasonRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                    status = getTagStatus(penalty1),
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

              "construct a card with correct messages including the expiry date for the point" in {

                val penalty1 = sampleRemovedPenaltyPoint

                mockTaxPeriodSummaryRow(penalty1)(Some(testTaxPeriodRow))
                mockExpiryReasonSummaryRow(penalty1)(Some(testExpiryReasonRow))

                lspSummaryListRowHelper.createLateSubmissionPenaltyCards(Seq(sampleRemovedPenaltyPoint), 2, 1) shouldBe
                  Seq(LateSubmissionPenaltySummaryCard(
                    cardRows = Seq(
                      testTaxPeriodRow,
                      testExpiryReasonRow
                    ),
                    cardTitle = messagesForLanguage.cardTitleRemovedPoint,
                    status = getTagStatus(penalty1),
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
        }
      }
    }
  }
}
