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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.LSPSummaryListRowHelper

trait MockLSPSummaryListRowHelper extends MockitoSugar {

  val testTaxPeriodRow: SummaryListRow = SummaryListRow(Key(Text("taxPeriod")), Value(Text("dateA to dateB")))
  val testTaxYearRow: SummaryListRow = SummaryListRow(Key(Text("taxYear")), Value(Text("dateA to dateB")))
  val testDueDateRow: SummaryListRow = SummaryListRow(Key(Text("dueDate")), Value(Text("date")))
  val testExpiryReasonRow: SummaryListRow = SummaryListRow(Key(Text("expiryReason")), Value(Text("reason")))
  val testReceivedDateRow: SummaryListRow = SummaryListRow(Key(Text("receivedDate")), Value(Text("date")))
  val testPointExpiryRow: SummaryListRow = SummaryListRow(Key(Text("expiryDate")), Value(Text("date")))
  val testAppealStatusRow: SummaryListRow = SummaryListRow(Key(Text("appealStatus")), Value(Text("status")))
  val testPayPenaltyByRow: SummaryListRow = SummaryListRow(Key(Text("payPenaltyBy")), Value(Text("1/4/2028")))

  lazy val mockLSPSummaryListRowHelper: LSPSummaryListRowHelper = mock[LSPSummaryListRowHelper]

  def mockMissingOrLateIncomeSourcesSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.missingOrLateIncomeSourcesSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockPayPenaltyByRow(penalty: LSPDetails, threshold: Int)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.payPenaltyByRow(eqTo(penalty), eqTo(threshold))(any()))
      .thenReturn(value)

  def mockTaxPeriodSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.taxPeriodSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockTaxYearSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.taxYearSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockDueDateSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.dueDateSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockExpiryReasonSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.expiryReasonSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockReceivedDateSummaryRow(penalty: LSPDetails)(value: SummaryListRow): OngoingStubbing[SummaryListRow] =
    when(mockLSPSummaryListRowHelper.receivedDateSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockPointExpiryDate(penalty: LSPDetails)(value: SummaryListRow): OngoingStubbing[SummaryListRow] =
    when(mockLSPSummaryListRowHelper.pointExpiryDate(eqTo(penalty))(any()))
      .thenReturn(value)

  def mockAppealStatusSummaryRow(appealStatus: Option[AppealStatusEnum.Value],
                                 appealLevel: Option[AppealLevelEnum.Value])(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.appealStatusRow(eqTo(appealStatus), eqTo(appealLevel))(any()))
      .thenReturn(value)

  def mockPenaltyStatusSummaryRow(penalty: LSPDetails)
                                 (value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.penaltyStatusRow(eqTo(penalty))(any()))
      .thenReturn(value)

}
