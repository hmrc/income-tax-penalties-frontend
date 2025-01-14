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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.lsp.LSPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.LSPSummaryListRowHelper

trait MockLSPSummaryListRowHelper extends MockitoSugar {

  val testTaxPeriodRow: SummaryListRow = SummaryListRow(Key(Text("taxPeriod")), Value(Text("dateA to dateB")))
  val testDueDateRow: SummaryListRow = SummaryListRow(Key(Text("dueDate")), Value(Text("date")))
  val testExpiryReasonRow: SummaryListRow = SummaryListRow(Key(Text("expiryReason")), Value(Text("reason")))
  val testReceivedDateRow: SummaryListRow = SummaryListRow(Key(Text("receivedDate")), Value(Text("date")))
  val testPointExpiryRow: SummaryListRow = SummaryListRow(Key(Text("expiryDate")), Value(Text("date")))
  val testAppealStatusRow: SummaryListRow = SummaryListRow(Key(Text("appealStatus")), Value(Text("status")))

  lazy val mockLSPSummaryListRowHelper: LSPSummaryListRowHelper = mock[LSPSummaryListRowHelper]

  def mockTaxPeriodSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.taxPeriodSummaryRow(eqTo(penalty))(any()))
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

  def mockAppealStatusSummaryRow(penalty: LSPDetails)(value: Option[SummaryListRow]): OngoingStubbing[Option[SummaryListRow]] =
    when(mockLSPSummaryListRowHelper.appealStatusSummaryRow(eqTo(penalty))(any()))
      .thenReturn(value)

}
