/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.{ComplianceData, ComplianceStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.TimelineEvent

import javax.inject.{Inject, Singleton}


@Singleton
class TimelineBuilderService @Inject()(timeMachine: TimeMachine) extends DateFormatter {

  def buildTimeline(complianceData: Option[ComplianceData])(implicit messages: Messages): Seq[TimelineEvent] =
    complianceData.fold(Seq.empty[TimelineEvent])(_.obligationDetails.filter(_.status == ComplianceStatusEnum.Open).map {
      data =>
        val isReturnLate = data.inboundCorrespondenceDueDate.isBefore(timeMachine.getCurrentDate)
        val isTaxYear = data.periodKey.contains("P0")
        // 17P0 signifies the tax return for 2017
        // 17P1 or 17G1 signifies the first quarter for 2017
        // 17P2 or 17G2 signifies the second quarter for 2017 etc

        TimelineEvent(
          headerContent = {
            val infix = if (isTaxYear) "tax.return" else "quarter"
            messages(s"compliance.timeline.$infix.heading", dateToString(data.inboundCorrespondenceFromDate), dateToString(data.inboundCorrespondenceToDate))
          },
          spanContent = {
            val suffix = if (isReturnLate) "submission.due.now" else "send.by"
            messages(s"compliance.timeline.$suffix", dateToString(data.inboundCorrespondenceDueDate))
          },
          tagContent = Option.when(isReturnLate)(messages("common.late"))
        )
    })
}
