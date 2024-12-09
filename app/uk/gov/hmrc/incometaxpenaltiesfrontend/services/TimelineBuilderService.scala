package uk.gov.hmrc.incometaxpenaltiesfrontend.services

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.compliance.{ComplianceData, ComplianceStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.TimelineEvent

import javax.inject.{Inject, Singleton}

@Singleton
class TimelineBuilderService @Inject()(timeMachine: TimeMachine) {

  def buildTimeline(complianceData: ComplianceData)(implicit messages: Messages): Seq[TimelineEvent] = {
    val filteredComplianceData = complianceData.obligationDetails.filter(_.status.equals(ComplianceStatusEnum.Open))
    if (filteredComplianceData.nonEmpty) {
      filteredComplianceData.map {
        data =>
          val isReturnLate = data.inboundCorrespondenceDueDate.isBefore(timeMachine.getCurrentDate)


      }

    } else {
      Html("")
    }

  }

}
