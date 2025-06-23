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
import fixtures.messages.{ExpiryReasonMessages, LSPCardMessages}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.ExpiryReasonEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter

class LSPSummaryListRowHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with LSPDetailsTestData with SummaryListRowHelper with DateFormatter {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lspSummaryListRowHelper: LSPSummaryListRowHelper = new LSPSummaryListRowHelper

  "LSPSummaryListRowHelper" when {

    Seq(
      (LSPCardMessages.English, ExpiryReasonMessages.English),
      (LSPCardMessages.Welsh, ExpiryReasonMessages.Welsh)
    ).foreach { case (messagesForLanguage, expiryMessages) =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"when language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .taxPeriodSummaryRow()" when {

          "there is a `from` and `to` date" should {

            "construct a SummaryListRow model for tax period with expected messages" in {

              lspSummaryListRowHelper.taxPeriodSummaryRow(sampleLateSubmissionPoint) shouldBe
                Some(summaryListRow(
                  label = messagesForLanguage.updatePeriod,
                  value = Html(messagesForLanguage.quarterValue(
                    dateToString(sampleLateSubmissionPoint.taxPeriodStartDate.get),
                    dateToString(sampleLateSubmissionPoint.taxPeriodEndDate.get)
                  ))
                ))
            }
          }

          "there is NOT a `from` and `to` date" should {

            "return None" in {

              lspSummaryListRowHelper.taxPeriodSummaryRow(sampleLateSubmissionPoint.copy(
                lateSubmissions = sampleLateSubmissionPoint.lateSubmissions.map(_.map(_.copy(
                  taxPeriodStartDate = None,
                  taxPeriodEndDate = None
                )))
              )) shouldBe None
            }
          }
        }

        "calling .dueDateSummaryRow()" should {

          "construct a SummaryListRow model for due date with expected messages" in {

            lspSummaryListRowHelper.dueDateSummaryRow(sampleLateSubmissionPoint) shouldBe
              Some(summaryListRow(
                label = messagesForLanguage.updateDueKey,
                value = Html(dateToString(sampleLateSubmissionPoint.dueDate.get))
              ))
          }
        }

        "calling .expiryReasonSummaryRow()" should {

          "construct a SummaryListRow model for expiry reason with expected messages" in {

            lspSummaryListRowHelper.expiryReasonSummaryRow(sampleLateSubmissionPoint.copy(
              expiryReason = Some(ExpiryReasonEnum.Appeal)
            )) shouldBe
              Some(summaryListRow(
                label = messagesForLanguage.expiryReasonKey,
                value = Html(expiryMessages.appeal)
              ))
          }
        }

        "calling .expiryReasonSummaryRow()" when {

          "return has been received" should {

            "construct a SummaryListRow model for received date with expected messages" in {

              lspSummaryListRowHelper.receivedDateSummaryRow(sampleLateSubmissionPoint) shouldBe
                summaryListRow(
                  label = messagesForLanguage.updateSubmittedKey,
                  value = Html(dateToString(sampleLateSubmissionPoint.receiptDate.get))
                )
            }
          }

          "return has NOT been received" should {

            "construct a SummaryListRow model for no return received with expected messages" in {

              lspSummaryListRowHelper.receivedDateSummaryRow(sampleLateSubmissionPoint.copy(
                lateSubmissions = sampleLateSubmissionPoint.lateSubmissions.map(_.map(_.copy(
                  returnReceiptDate = None
                )))
              )) shouldBe
                summaryListRow(
                  label = messagesForLanguage.updateSubmittedKey,
                  value = Html(messagesForLanguage.returnNotReceived)
                )
            }
          }
        }

        "calling .pointExpiryDate()" should {

          "construct a SummaryListRow model for point expiry date with expected messages" in {

            lspSummaryListRowHelper.pointExpiryDate(sampleLateSubmissionPoint) shouldBe
              summaryListRow(
                messagesForLanguage.expiryDateKey,
                Html(dateToString(sampleLateSubmissionPoint.penaltyExpiryDate))
              )
          }
        }
      }
    }
  }
}
