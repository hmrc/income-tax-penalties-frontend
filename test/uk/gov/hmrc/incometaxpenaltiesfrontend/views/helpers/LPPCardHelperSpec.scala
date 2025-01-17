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

import fixtures.LPPDetailsTestData
import fixtures.messages.LPPCardMessages
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{CurrencyFormatter, DateFormatter}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LatePaymentPenaltySummaryCard
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.mocks.MockLPPSummaryListRowHelper

class LPPCardHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with DateFormatter
  with LPPDetailsTestData with MockLPPSummaryListRowHelper with TagHelper with SummaryListRowHelper {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lppSummaryListRowHelper: LPPCardHelper = new LPPCardHelper(mockLPPSummaryListRowHelper)

  "LPPCardHelper" when {

    Seq(LPPCardMessages.English, LPPCardMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .createLateSubmissionPenaltyCards()" when {

          "rendering a LPP1 or LPP2" when {

            "is NOT being appealed" should {

              "construct a card with correct messages and summary rows" in {

                val penalty1 = sampleUnpaidLPP1

                mockPenaltyTypeRow(penalty1)(testPenaltyType)
                mockIncomeTaxPeriodRow(penalty1)(testTaxPeriodRow)
                mockIncomeTaxDueRow(penalty1)(testDueDateRow)
                mockIncomeTaxPaymentDateRow(penalty1)(testPaymentDateRow)
                mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(None)

                lppSummaryListRowHelper.createLatePaymentPenaltyCards(Seq(penalty1 -> 1)) shouldBe
                  Seq(LatePaymentPenaltySummaryCard(
                    index = 1,
                    cardTitle = messagesForLanguage.cardTitlePenalty(CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty1.amountDue)),
                    cardRows = Seq(
                      testPenaltyType,
                      testTaxPeriodRow,
                      testDueDateRow,
                      testPaymentDateRow
                    ),
                    status = getTagStatus(penalty1),
                    penaltyChargeReference = penalty1.penaltyChargeReference,
                    principalChargeReference = penalty1.principalChargeReference,
                    isPenaltyPaid = penalty1.isPaid,
                    amountDue = penalty1.amountDue,
                    appealStatus = penalty1.appealStatus,
                    appealLevel = penalty1.appealLevel,
                    incomeTaxIsPaid = penalty1.incomeTaxIsPaid,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = dateToString(penalty1.principalChargeDueDate),
                    taxPeriodStartDate = penalty1.principalChargeBillingFrom.toString,
                    taxPeriodEndDate = penalty1.principalChargeBillingTo.toString,
                    incomeTaxOutstandingAmountInPence = penalty1.incomeTaxOutstandingAmountInPence,
                    isTTPActive = false
                  ))
              }
            }

            "is being appealed" should {

              "construct a card with correct messages and summary rows including an Appeal Status" in {

                val penalty1 = sampleLPP1AppealUnpaid(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC)

                mockPenaltyTypeRow(penalty1)(testPenaltyType)
                mockIncomeTaxPeriodRow(penalty1)(testTaxPeriodRow)
                mockIncomeTaxDueRow(penalty1)(testDueDateRow)
                mockIncomeTaxPaymentDateRow(penalty1)(testPaymentDateRow)
                mockAppealStatusSummaryRow(penalty1.appealStatus, penalty1.appealLevel)(Some(testAppealStatusRow))

                lppSummaryListRowHelper.createLatePaymentPenaltyCards(Seq(penalty1 -> 1)) shouldBe
                  Seq(LatePaymentPenaltySummaryCard(
                    index = 1,
                    cardTitle = messagesForLanguage.cardTitlePenalty(CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty1.amountDue)),
                    cardRows = Seq(
                      testPenaltyType,
                      testTaxPeriodRow,
                      testDueDateRow,
                      testPaymentDateRow,
                      testAppealStatusRow
                    ),
                    status = getTagStatus(penalty1),
                    penaltyChargeReference = penalty1.penaltyChargeReference,
                    principalChargeReference = penalty1.principalChargeReference,
                    isPenaltyPaid = penalty1.isPaid,
                    amountDue = penalty1.amountDue,
                    appealStatus = penalty1.appealStatus,
                    appealLevel = penalty1.appealLevel,
                    incomeTaxIsPaid = penalty1.incomeTaxIsPaid,
                    penaltyCategory = penalty1.penaltyCategory,
                    dueDate = dateToString(penalty1.principalChargeDueDate),
                    taxPeriodStartDate = penalty1.principalChargeBillingFrom.toString,
                    taxPeriodEndDate = penalty1.principalChargeBillingTo.toString,
                    incomeTaxOutstandingAmountInPence = penalty1.incomeTaxOutstandingAmountInPence,
                    isTTPActive = false
                  ))
              }
            }
          }

          "rendering a MANUAL penalty" should {

            "construct a card with correct messages and summary rows including an Appeal Status" in {

              val penalty1 = sampleManualLPP

              mockPenaltyTypeRow(penalty1)(testPenaltyType)
              mockAddedOnRow(penalty1)(Some(testAddedOnRow))

              lppSummaryListRowHelper.createLatePaymentPenaltyCards(Seq(penalty1 -> 1)) shouldBe
                Seq(LatePaymentPenaltySummaryCard(
                  index = 1,
                  cardTitle = messagesForLanguage.cardTitlePenalty(CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty1.amountDue)),
                  cardRows = Seq(
                    testPenaltyType,
                    testAddedOnRow
                  ),
                  status = getTagStatus(penalty1),
                  penaltyChargeReference = penalty1.penaltyChargeReference,
                  principalChargeReference = penalty1.principalChargeReference,
                  isPenaltyPaid = penalty1.isPaid,
                  amountDue = penalty1.amountDue,
                  appealStatus = penalty1.appealStatus,
                  appealLevel = penalty1.appealLevel,
                  incomeTaxIsPaid = penalty1.incomeTaxIsPaid,
                  penaltyCategory = penalty1.penaltyCategory,
                  dueDate = dateToString(penalty1.principalChargeDueDate),
                  taxPeriodStartDate = penalty1.principalChargeBillingFrom.toString,
                  taxPeriodEndDate = penalty1.principalChargeBillingTo.toString,
                  incomeTaxOutstandingAmountInPence = penalty1.incomeTaxOutstandingAmountInPence,
                  isTTPActive = false
                ))
            }
          }
        }
      }
    }
  }
}
