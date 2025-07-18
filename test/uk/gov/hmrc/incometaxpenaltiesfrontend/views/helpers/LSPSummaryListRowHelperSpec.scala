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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.{ExpiryReasonEnum, LSPPenaltyStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter

import java.time.LocalDate

class LSPSummaryListRowHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with LSPDetailsTestData with SummaryListRowHelper with DateFormatter {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val lspSummaryListRowHelper: LSPSummaryListRowHelper = new LSPSummaryListRowHelper

  "LSPSummaryListRowHelper" that {

    Seq(
      (LSPCardMessages.English, ExpiryReasonMessages.English),
      (LSPCardMessages.Welsh, ExpiryReasonMessages.Welsh)
    ).foreach { case (messagesForLanguage, expiryMessages) =>

      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang(messagesForLanguage.lang.code)))

      s"has language is set to '${messagesForLanguage.lang.name}'" when {

        "calling .missingOrLateIncomeSourcesSummaryRow" should {

          "construct a SummaryListRow model with 1 income source" when {
            "the penalty due date is not 31st Jan and the LSPDetails contains 1 incomeSource" in {
              val lateSubmission1 = lateSubmission.copy(incomeSource = Some("Income Source 1"))
              val lspDetails = sampleLateSubmissionPoint.copy(lateSubmissions = Some(Seq(lateSubmission1)))

              val res = lspSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(lspDetails)
              val expectedResult = Some(summaryListRow(
                label = messagesForLanguage.missingOrLateIncomeSources,
                value = Html(
                  s"""<ul class="govuk-list govuk-list--bullet">
                     |  <li>Income Source 1</li>
                     |</ul>""".stripMargin
                )
              ))

              res shouldBe expectedResult
            }
          }

          "construct a SummaryListRow model with 2 income source" when {
            "the penalty due date is not 31st Jan and the LSPDetails contains 3 lateSubmissions with 2 containing a incomeSource" in {
              val lateSubmission1 = lateSubmission.copy(incomeSource = Some("Income Source 1"))
              val lateSubmission2 = lateSubmission.copy(incomeSource = None)
              val lateSubmission3 = lateSubmission.copy(incomeSource = Some("Income Source 2"))
              val lspDetails = sampleLateSubmissionPoint.copy(lateSubmissions = Some(Seq(lateSubmission1, lateSubmission2, lateSubmission3)))

              val res = lspSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(lspDetails)
              val expectedResult = Some(summaryListRow(
                label = messagesForLanguage.missingOrLateIncomeSources,
                value = Html(
                  s"""<ul class="govuk-list govuk-list--bullet">
                     |  <li>Income Source 1</li><li>Income Source 2</li>
                     |</ul>""".stripMargin
                )
              ))

              res shouldBe expectedResult
            }
          }

          "return None" when {
            "the penalty due date is the 31st Jan and the user has income sources" in {
              val lateSubmission1 = lateSubmission.copy(
                incomeSource = Some("Income Source 1"),
                taxPeriodDueDate = Some(LocalDate.of(2024, 1, 31))
              )
              val lspDetails = sampleLateSubmissionPoint.copy(
                lateSubmissions = Some(Seq(lateSubmission1)))

              val res = lspSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(lspDetails)

              res shouldBe None
            }

            "the penalty due date is the 31st Jan and the user has no income sources" in {
              val lateSubmission1 = lateSubmission.copy(
                incomeSource = None,
                taxPeriodDueDate = Some(LocalDate.of(2024, 1, 31))
              )
              val lspDetails = sampleLateSubmissionPoint.copy(
                lateSubmissions = Some(Seq(lateSubmission1)))

              val res = lspSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(lspDetails)

              res shouldBe None
            }

            "the penalty due date is not 31st Jan and the user has no income sources" in {
              val lateSubmission1 = lateSubmission.copy(
                incomeSource = None,
                taxPeriodDueDate = Some(LocalDate.of(2024, 8, 20))
              )
              val lspDetails = sampleLateSubmissionPoint.copy(
                lateSubmissions = Some(Seq(lateSubmission1)))

              val res = lspSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(lspDetails)

              res shouldBe None
            }
          }
        }

        "calling .taxPeriodSummaryRow()" when {

          "there is a `from` and `to` date" should {

            "construct a SummaryListRow model for tax period with expected messages when it is a late update" in {

              lspSummaryListRowHelper.taxPeriodSummaryRow(sampleLateSubmissionPoint) shouldBe
                Some(summaryListRow(
                  label = messagesForLanguage.updatePeriod,
                  value = Html(messagesForLanguage.quarterValue(
                    dateToString(sampleLateSubmissionPoint.taxPeriodStartDate.get),
                    dateToString(sampleLateSubmissionPoint.taxPeriodEndDate.get)
                  ))
                ))
            }

            "do not display a SummaryListRow model for tax period when it is not a late update" in {

              val lspDetailsAnnual = sampleLateSubmissionPoint.copy(
                lateSubmissions = sampleLateSubmissionPoint.lateSubmissions.map(_.map(_.copy(
                  taxPeriodDueDate = Some(LocalDate.of(2021, 1, 31))
                )))
              )

              lspSummaryListRowHelper.taxPeriodSummaryRow(lspDetailsAnnual) shouldBe None
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

        "calling .payPenaltyByRow()" should {
          "construct a SummaryListRow model for pay penalty by with expected messages" in {
            val lspDetailsAnnual = sampleLateSubmissionPoint.copy(
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              penaltyOrder = Some("3"),
              chargeDueDate = Some(chargeDueDate)
            )

            lspSummaryListRowHelper.payPenaltyByRow(lspDetailsAnnual, 2) shouldBe
              Some(summaryListRow(
                label = messagesForLanguage.payPenaltyBy,
                value = Html(dateToString(chargeDueDate))
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
