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
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}

import java.time.LocalDate

class LPPSummaryListRowHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with LPPDetailsTestData with SummaryListRowHelper with DateFormatter {

  class Setup(runDate: LocalDate = LocalDate.of(2023, 4, 13)) {
    object FakeTimeMachine extends TimeMachine(appConfig = app.injector.instanceOf[AppConfig]) {
      override def getCurrentDate(): LocalDate = runDate
    }
    implicit val tm: TimeMachine = FakeTimeMachine
  }
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lspSummaryListRowHelper: LPPSummaryListRowHelper = new LPPSummaryListRowHelper

  "LPPSummaryListRowHelper" when {

    Seq(LPPCardMessages.English, LPPCardMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .addedOnRow()" when {

          "construct a SummaryListRow model for the Manually added penalty with expected messages" in {

            lspSummaryListRowHelper.addedOnRow(sampleManualLPP) shouldBe
              Some(summaryListRow(
                label = messagesForLanguage.addedOnKey,
                value = Html(dateToString(sampleManualLPP.penaltyChargeCreationDate.get))
              ))
          }
        }

        "calling .incomeTaxPeriodRow()" when {

          "construct a SummaryListRow model for tax period with expected messages" in {

            lspSummaryListRowHelper.incomeTaxPeriodRow(samplePaidLPP1) shouldBe
              summaryListRow(
                label = messagesForLanguage.incomeTaxPeriodKey,
                value = Html(messagesForLanguage.overdueChargeValue(
                  samplePaidLPP1.principalChargeBillingFrom.getYear.toString,
                  samplePaidLPP1.principalChargeBillingTo.getYear.toString
                ))
              )
          }
        }

        "calling .incomeTaxDueRow()" when {

          "construct a SummaryListRow model for tax due date with expected messages" in {

            lspSummaryListRowHelper.incomeTaxDueRow(samplePaidLPP1) shouldBe
              summaryListRow(
                label = messagesForLanguage.incomeTaxDueKey,
                value = Html(dateToString(samplePaidLPP1.principalChargeDueDate))
              )
          }
        }

        "calling .incomeTaxPaymentDateRow()" when {

          "penalty has been paid" when {

            "construct a SummaryListRow model for payment received date with expected messages" in new Setup(){

              lspSummaryListRowHelper.incomeTaxPaymentDateRow(samplePaidLPP1) shouldBe
                summaryListRow(
                  label = messagesForLanguage.incomeTaxPaidKey,
                  value = Html(dateToString(samplePaidLPP1.principalChargeLatestClearing.get))
                )
            }
          }

          "penalty has NOT been paid" when {

            "the penalty has a proposed payment plan" should {
              "construct a SummaryListRow model for payment received date with expected messages" in new Setup(LocalDate.of(2021, 7, 1)) {

                lspSummaryListRowHelper.incomeTaxPaymentDateRow(sampleUnpaidLPP1ProposedPaymentPlan) shouldBe
                  summaryListRow(
                    label = messagesForLanguage.incomeTaxPaidKey,
                    value = Html(messagesForLanguage.paymentPlanProposed)
                  )
              }
            }
            "the penalty has an agreed payment plan" should {
              "construct a SummaryListRow model for payment received date with expected messages" in new Setup(LocalDate.of(2021, 7, 1)) {

                lspSummaryListRowHelper.incomeTaxPaymentDateRow(sampleUnpaidLPP1AgreedPaymentPlan) shouldBe
                  summaryListRow(
                    label = messagesForLanguage.incomeTaxPaidKey,
                    value = Html(messagesForLanguage.paymentPlanAgreed)
                  )
              }
            }
            "the penalty has no payment plan" should {
              "construct a SummaryListRow model for payment received date with expected messages" in new Setup() {

                lspSummaryListRowHelper.incomeTaxPaymentDateRow(sampleUnpaidLPP1) shouldBe
                  summaryListRow(
                    label = messagesForLanguage.incomeTaxPaidKey,
                    value = Html(messagesForLanguage.paymentNotReceived)
                  )
              }
            }
          }
        }
      }
    }
  }
}
