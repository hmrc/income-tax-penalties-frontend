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

  def buildTimeline(complianceData: ComplianceData)(implicit messages: Messages): Seq[TimelineEvent] = {
    val filteredComplianceData = complianceData.obligationDetails.filter(_.status.equals(ComplianceStatusEnum.Open))
    if (filteredComplianceData.nonEmpty) {
      filteredComplianceData.map {
        data =>
          val isReturnLate = data.inboundCorrespondenceDueDate.isBefore(timeMachine.getCurrentDate)

          TimelineEvent(
            headerContent = messages("compliance.timeline.quarter.heading", dateToString(data.inboundCorrespondenceFromDate),
              dateToString(data.inboundCorrespondenceToDate)),
            spanContent =
              if (isReturnLate) messages("compliance.timeline.submission.due.now", dateToString(data.inboundCorrespondenceDueDate))
              else messages("compliance.timeline.send.by", dateToString(data.inboundCorrespondenceDueDate)),
            tagContent = if (isReturnLate) Some(messages("general.tag.late")) else None
          )
      }
    }
    else { Seq.empty }
  }
}
