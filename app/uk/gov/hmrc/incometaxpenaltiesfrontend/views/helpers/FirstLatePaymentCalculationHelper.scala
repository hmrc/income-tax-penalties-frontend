package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.FirstLatePaymentPenaltyCalculationData

class FirstLatePaymentCalculationHelper {


  def getPaymentDetails(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): Option[String] = {

    if(calculationData.llpHRCharge.isEmpty && !calculationData.incomeTaxIsPaid) {
      None
    } else {
      Some{
        if(calculationData.isPenaltyPaid) {
          messages("calculation.individual.paid.penalty.on", "22 December 2024")
        } else {
          messages("calculation.individual.pay.penalty.by", DateFormatter.dateToString(calculationData.payPenaltyBy))
        }
      }
    }
  }

  def getMissedDeadlineMsg(calculationData: FirstLatePaymentPenaltyCalculationData,
                        isAgent: Boolean)(implicit messages: Messages): String = {

    calculationData.llpHRCharge match {
      case Some(_) =>  messages("calculation.payment.missed.reason.additional")
      case None if !calculationData.incomeTaxIsPaid =>
        messages("calculation.individual.payment.15.30.missed.reason.taxUnpaid")
      case _ => messages("calculation.individual.payment.15.30.missed.reason")
    }
  }

}
