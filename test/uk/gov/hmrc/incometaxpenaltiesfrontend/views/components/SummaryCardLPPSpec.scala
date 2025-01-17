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

import fixtures.LPPDetailsTestData
import fixtures.messages.{LPPCardMessages, PenaltyTagStatusMessages}
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{CurrencyFormatter, DateFormatter}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LatePaymentPenaltySummaryCard
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.TagHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.components.SummaryCardLPP

class SummaryCardLPPSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite
  with LPPDetailsTestData with TagHelper with DateFormatter {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val summaryCard: SummaryCardLPP = app.injector.instanceOf[SummaryCardLPP]

  "SummaryCardLPP" when {

    Seq(
      LPPCardMessages.English -> PenaltyTagStatusMessages.English,
      LPPCardMessages.Welsh -> PenaltyTagStatusMessages.Welsh
    ).foreach { case (messagesForLanguage, penaltyStatusMessages) =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"rendering in language '${messagesForLanguage.lang.name}'" when {

        "calling .apply()" when {

          "provided with a Late Payment Penalty model" when {

            "penalty category is 'MANUAL'" should {

              "generate a Summary Card with a cannot appeal message" in {

                val penalty = sampleManualLPP
                val amount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.penaltyAmountPosted)

                val summaryCardHtml = summaryCard(LatePaymentPenaltySummaryCard(
                  index = 1,
                  cardTitle = messagesForLanguage.cardTitlePenalty(amount),
                  cardRows = Seq.empty,
                  status = getTagStatus(penalty),
                  penaltyChargeReference = penalty.penaltyChargeReference,
                  principalChargeReference = penalty.principalChargeReference,
                  isPenaltyPaid = penalty.isPaid,
                  amountDue = penalty.penaltyAmountPosted,
                  incomeTaxIsPaid = penalty.principalChargeLatestClearing.isDefined,
                  penaltyCategory = penalty.penaltyCategory,
                  dueDate = dateToString(penalty.principalChargeDueDate),
                  taxPeriodStartDate = dateToString(penalty.principalChargeBillingFrom),
                  taxPeriodEndDate = dateToString(penalty.principalChargeBillingTo),
                  incomeTaxOutstandingAmountInPence = penalty.incomeTaxOutstandingAmountInPence
                ))

                val document = Jsoup.parse(summaryCardHtml.toString)

                document.select("h2").text() shouldBe messagesForLanguage.cardTitlePenalty(amount)
                document.select("#lpp-status-1").text() shouldBe penaltyStatusMessages.due
                document.select("#lpp-actions-1").text() shouldBe messagesForLanguage.cannotAppeal
              }
            }

            "penalty category is NOT 'MANUAL'" when {

              "Income Tax amount has NOT been paid AND NO Time to Pay arrangement exists" when {

                "a penalty appeal is in progress" should {

                  "generate a Summary Card with only calculation link" in {

                    val penalty = sampleLPP1AppealUnpaid(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC)
                    val amount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.penaltyAmountPosted)
                    val amountOutstanding = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.penaltyAmountOutstanding.get)

                    val summaryCardHtml = summaryCard(LatePaymentPenaltySummaryCard(
                      index = 1,
                      cardTitle = messagesForLanguage.cardTitlePenalty(amount),
                      cardRows = Seq.empty,
                      status = getTagStatus(penalty),
                      penaltyChargeReference = penalty.penaltyChargeReference,
                      principalChargeReference = penalty.principalChargeReference,
                      isPenaltyPaid = penalty.isPaid,
                      amountDue = penalty.penaltyAmountPosted,
                      incomeTaxIsPaid = penalty.principalChargeLatestClearing.isDefined,
                      penaltyCategory = penalty.penaltyCategory,
                      dueDate = dateToString(penalty.principalChargeDueDate),
                      taxPeriodStartDate = dateToString(penalty.principalChargeBillingFrom),
                      taxPeriodEndDate = dateToString(penalty.principalChargeBillingTo),
                      incomeTaxOutstandingAmountInPence = penalty.incomeTaxOutstandingAmountInPence,
                      appealLevel = Some(AppealLevelEnum.HMRC),
                      appealStatus = Some(AppealStatusEnum.Under_Appeal)
                    ))

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h2").text() shouldBe messagesForLanguage.cardTitlePenalty(amount)
                    document.select("#lpp-status-1").text() shouldBe penaltyStatusMessages.amountDue(amountOutstanding)
                    document.select("#lpp-view-calculation-link-1").text() shouldBe messagesForLanguage.cardLinksViewCalculation
                    document.select("#lpp-appeal-link-1").isEmpty shouldBe true
                  }
                }

                "a penalty appeal is NOT in progress" should {

                  "generate a Summary Card with a Calculation and Find Out How to appeal link" in {

                    val penalty = sampleUnpaidLPP1
                    val amount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.penaltyAmountPosted)

                    val summaryCardHtml = summaryCard(LatePaymentPenaltySummaryCard(
                      index = 1,
                      cardTitle = messagesForLanguage.cardTitlePenalty(amount),
                      cardRows = Seq.empty,
                      status = getTagStatus(penalty),
                      penaltyChargeReference = penalty.penaltyChargeReference,
                      principalChargeReference = penalty.principalChargeReference,
                      isPenaltyPaid = penalty.isPaid,
                      amountDue = penalty.penaltyAmountPosted,
                      incomeTaxIsPaid = penalty.principalChargeLatestClearing.isDefined,
                      penaltyCategory = penalty.penaltyCategory,
                      dueDate = dateToString(penalty.principalChargeDueDate),
                      taxPeriodStartDate = dateToString(penalty.principalChargeBillingFrom),
                      taxPeriodEndDate = dateToString(penalty.principalChargeBillingTo),
                      incomeTaxOutstandingAmountInPence = penalty.incomeTaxOutstandingAmountInPence
                    ))

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h2").text() shouldBe messagesForLanguage.cardTitlePenalty(amount)
                    document.select("#lpp-status-1").text() shouldBe penaltyStatusMessages.estimate
                    document.select("#lpp-view-calculation-link-1").text() shouldBe messagesForLanguage.cardLinksViewCalculation
                    document.select("#lpp-appeal-link-1").text() shouldBe messagesForLanguage.cardLinksFindOutHowToAppeal
                  }
                }
              }

              "Income Tax amount has been paid" when {

                "a penalty appeal is in progress" should {

                  "generate a Summary Card with only the calculation link" in {

                    val penalty = sampleLPP1AppealPaid(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC)
                    val amount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.penaltyAmountPosted)

                    val summaryCardHtml = summaryCard(LatePaymentPenaltySummaryCard(
                      index = 1,
                      cardTitle = messagesForLanguage.cardTitlePenalty(amount),
                      cardRows = Seq.empty,
                      status = getTagStatus(penalty),
                      penaltyChargeReference = penalty.penaltyChargeReference,
                      principalChargeReference = penalty.principalChargeReference,
                      isPenaltyPaid = penalty.isPaid,
                      amountDue = penalty.penaltyAmountPosted,
                      incomeTaxIsPaid = penalty.principalChargeLatestClearing.isDefined,
                      penaltyCategory = penalty.penaltyCategory,
                      dueDate = dateToString(penalty.principalChargeDueDate),
                      taxPeriodStartDate = dateToString(penalty.principalChargeBillingFrom),
                      taxPeriodEndDate = dateToString(penalty.principalChargeBillingTo),
                      incomeTaxOutstandingAmountInPence = penalty.incomeTaxOutstandingAmountInPence,
                      appealLevel = Some(AppealLevelEnum.HMRC),
                      appealStatus = Some(AppealStatusEnum.Under_Appeal)
                    ))

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h2").text() shouldBe messagesForLanguage.cardTitlePenalty(amount)
                    document.select("#lpp-status-1").text() shouldBe penaltyStatusMessages.paid
                    document.select("#lpp-view-calculation-link-1").text() shouldBe messagesForLanguage.cardLinksViewCalculation
                    document.select("#lpp-appeal-link-1").isEmpty shouldBe true
                  }
                }

                "a penalty appeal is NOT in progress" should {

                  "generate a Summary Card with a Calculation and an appeal link" in {

                    val penalty = samplePaidLPP1
                    val amount = CurrencyFormatter.parseBigDecimalNoPaddedZeroToFriendlyValue(penalty.penaltyAmountPosted)

                    val summaryCardHtml = summaryCard(LatePaymentPenaltySummaryCard(
                      index = 1,
                      cardTitle = messagesForLanguage.cardTitlePenalty(amount),
                      cardRows = Seq.empty,
                      status = getTagStatus(penalty),
                      penaltyChargeReference = penalty.penaltyChargeReference,
                      principalChargeReference = penalty.principalChargeReference,
                      isPenaltyPaid = penalty.isPaid,
                      amountDue = penalty.penaltyAmountPosted,
                      incomeTaxIsPaid = penalty.principalChargeLatestClearing.isDefined,
                      penaltyCategory = penalty.penaltyCategory,
                      dueDate = dateToString(penalty.principalChargeDueDate),
                      taxPeriodStartDate = dateToString(penalty.principalChargeBillingFrom),
                      taxPeriodEndDate = dateToString(penalty.principalChargeBillingTo),
                      incomeTaxOutstandingAmountInPence = penalty.incomeTaxOutstandingAmountInPence
                    ))

                    val document = Jsoup.parse(summaryCardHtml.toString)

                    document.select("h2").text() shouldBe messagesForLanguage.cardTitlePenalty(amount)
                    document.select("#lpp-status-1").text() shouldBe penaltyStatusMessages.paid
                    document.select("#lpp-view-calculation-link-1").text() shouldBe messagesForLanguage.cardLinksViewCalculation
                    document.select("#lpp-appeal-link-1").text() shouldBe messagesForLanguage.cardLinksAppealThisPenalty
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
