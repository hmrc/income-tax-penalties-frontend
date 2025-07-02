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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyCategoryEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter

class LPPSummaryListRowHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with LPPDetailsTestData with SummaryListRowHelper with DateFormatter {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lspSummaryListRowHelper: LPPSummaryListRowHelper = new LPPSummaryListRowHelper

  "LPPSummaryListRowHelper" when {

    Seq(LPPCardMessages.English, LPPCardMessages.Welsh).foreach { messagesForLanguage =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .penaltyTypeRow()" when {

          Seq(LPPPenaltyCategoryEnum.LPP1, LPPPenaltyCategoryEnum.LPP2, LPPPenaltyCategoryEnum.MANUAL).foreach { penaltyType =>

            s"when penalty type is '$penaltyType'" should {

              "construct a SummaryListRow model for the penalty type with expected messages" in {

                lspSummaryListRowHelper.penaltyTypeRow(samplePaidLPP1.copy(penaltyCategory = penaltyType)) shouldBe
                  summaryListRow(
                    label = messagesForLanguage.penaltyTypeKey,
                    value = Html(messagesForLanguage.penaltyTypeValue(penaltyType))
                  )
              }
            }
          }
        }

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

            "construct a SummaryListRow model for payment received date with expected messages" in {

              lspSummaryListRowHelper.incomeTaxPaymentDateRow(samplePaidLPP1) shouldBe
                summaryListRow(
                  label = messagesForLanguage.incomeTaxPaidKey,
                  value = Html(dateToString(samplePaidLPP1.principalChargeLatestClearing.get))
                )
            }
          }

          "penalty has NOT been paid" when {

            "construct a SummaryListRow model for payment received date with expected messages" in {

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
