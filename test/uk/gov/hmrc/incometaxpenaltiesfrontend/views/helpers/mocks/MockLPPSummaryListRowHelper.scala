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
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPDetails
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers.LPPSummaryListRowHelper

trait MockLPPSummaryListRowHelper extends MockFactory {
  this: TestSuite =>

  val testPenaltyType: SummaryListRow = SummaryListRow(Key(Text("penaltyType")), Value(Text("LPP1")))
  val testAddedOnRow: SummaryListRow = SummaryListRow(Key(Text("addedOn")), Value(Text("date")))
  val testTaxPeriodRow: SummaryListRow = SummaryListRow(Key(Text("taxPeriod")), Value(Text("dateA to dateB")))
  val testDueDateRow: SummaryListRow = SummaryListRow(Key(Text("dueDate")), Value(Text("date")))
  val testPaymentDateRow: SummaryListRow = SummaryListRow(Key(Text("paymentDate")), Value(Text("date")))
  val testAppealStatusRow: SummaryListRow = SummaryListRow(Key(Text("appealStatus")), Value(Text("status")))

  lazy val mockLPPSummaryListRowHelper: LPPSummaryListRowHelper = mock[LPPSummaryListRowHelper]

  def mockAddedOnRow(penalty: LPPDetails)(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] =
    (mockLPPSummaryListRowHelper.addedOnRow(_:LPPDetails)(_:Messages))
      .expects(penalty, *)
      .returning(value)

  def mockIncomeTaxPeriodRow(penalty: LPPDetails)(value: SummaryListRow): CallHandler[SummaryListRow] =
    (mockLPPSummaryListRowHelper.incomeTaxPeriodRow(_:LPPDetails)(_:Messages))
      .expects(penalty, *)
      .returning(value)

  def mockIncomeTaxDueRow(penalty: LPPDetails)(value: SummaryListRow): CallHandler[SummaryListRow] =
    (mockLPPSummaryListRowHelper.incomeTaxDueRow(_:LPPDetails)(_:Messages))
      .expects(penalty, *)
      .returning(value)

  def mockIncomeTaxPaymentDateRow(penalty: LPPDetails)(value: SummaryListRow): CallHandler[SummaryListRow] =
    (mockLPPSummaryListRowHelper.incomeTaxPaymentDateRow(_:LPPDetails)(_:Messages))
      .expects(penalty, *)
      .returning(value)

  def mockAppealStatusSummaryRow(appealStatus: Option[AppealStatusEnum.Value],
                                 appealLevel: Option[AppealLevelEnum.Value])(value: Option[SummaryListRow]): CallHandler[Option[SummaryListRow]] =
    (mockLPPSummaryListRowHelper.appealStatusRow(_:Option[AppealStatusEnum.Value], _:Option[AppealLevelEnum.Value])(_:Messages))
      .expects(appealStatus, appealLevel, *)
      .returning(value)

  def mockPayPenaltyByRow(penalty: LPPDetails)(value: Option[SummaryListRow])
  : CallHandler[Option[SummaryListRow]] =
    (mockLPPSummaryListRowHelper.payPenaltyByRow(_:LPPDetails)(_:Messages))
      .expects(penalty, *)
      .returning(value)

}
