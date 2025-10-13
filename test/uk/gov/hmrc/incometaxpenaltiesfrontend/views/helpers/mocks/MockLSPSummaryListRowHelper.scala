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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.LSPSummaryListRowHelper

trait MockLSPSummaryListRowHelper extends MockFactory {
  _: TestSuite =>

  val testTaxPeriodRow: SummaryListRow = SummaryListRow(Key(Text("taxPeriod")), Value(Text("dateA to dateB")))
  val testTaxYearRow: SummaryListRow = SummaryListRow(Key(Text("taxYear")), Value(Text("dateA to dateB")))
  val testDueDateRow: SummaryListRow = SummaryListRow(Key(Text("dueDate")), Value(Text("date")))
  val testReceivedDateRow: SummaryListRow = SummaryListRow(Key(Text("receivedDate")), Value(Text("date")))
  val testPointExpiryRow: SummaryListRow = SummaryListRow(Key(Text("expiryDate")), Value(Text("date")))
  val testPointExpiredOnRow: SummaryListRow = SummaryListRow(Key(Text("pointExpiredOn")), Value(Text("date")))
  val testAppealStatusRow: SummaryListRow = SummaryListRow(Key(Text("appealStatus")), Value(Text("status")))
  val testPayPenaltyByRow: SummaryListRow = SummaryListRow(Key(Text("payPenaltyBy")), Value(Text("1/4/2028")))

  lazy val mockLSPSummaryListRowHelper: LSPSummaryListRowHelper = mock[LSPSummaryListRowHelper]

  def mockMissingOrLateIncomeSourcesSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] = {
    (mockLSPSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(value)
  }

  def mockPayPenaltyByRow(penalty: LSPDetails, threshold: Int)(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] = {
    (mockLSPSummaryListRowHelper.payPenaltyByRow(_: LSPDetails, _: Int)(_: Messages))
      .expects(penalty, threshold, *)
      .returning(value)
  }

  def mockTaxPeriodSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] =
    (mockLSPSummaryListRowHelper.taxPeriodSummaryRow(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(value)

  def mockTaxYearSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] =
    (mockLSPSummaryListRowHelper.taxYearSummaryRow(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(value)

  def mockDueDateSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] =
    (mockLSPSummaryListRowHelper.dueDateSummaryRow(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(value)

  def mockReceivedDateSummaryRow(penalty: LSPDetails)(value: SummaryListRow): CallHandler[SummaryListRow] =
    (mockLSPSummaryListRowHelper.receivedDateSummaryRow(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(value)

  def mockPointExpiryDate(penalty: LSPDetails)(value: SummaryListRow): CallHandler[SummaryListRow] =
    (mockLSPSummaryListRowHelper.pointExpiryDate(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(value)

  def mockPointExpiredOnRow(penalty: LSPDetails)(value: SummaryListRow): CallHandler[Option[SummaryListRow]] =
    (mockLSPSummaryListRowHelper.pointExpiredOnRow(_: LSPDetails)(_: Messages))
      .expects(penalty, *)
      .returning(Some(value))

  def mockAppealStatusSummaryRow(appealStatus: Option[AppealStatusEnum.Value],
                                 appealLevel: Option[AppealLevelEnum.Value])(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] =
    (mockLSPSummaryListRowHelper.appealStatusRow(_:Option[AppealStatusEnum.Value], _:Option[AppealLevelEnum.Value])(_:Messages))
      .expects(appealStatus, appealLevel, *)
      .returning(value)

}
