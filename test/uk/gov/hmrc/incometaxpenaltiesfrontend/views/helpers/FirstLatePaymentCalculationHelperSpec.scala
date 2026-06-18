/*
 * Copyright 2026 HM Revenue & Customs
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

import fixtures.PenaltiesDetailsTestData
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import java.time.LocalDate
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.LLPCharge
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.FirstLatePaymentPenaltyCalculationData
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.breathingSpace.BreathingSpace

class FirstLatePaymentCalculationHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite
  with PenaltiesDetailsTestData {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))

  val helper = new FirstLatePaymentCalculationHelper

  private def withTaxYear2027(d: FirstLatePaymentPenaltyCalculationData): FirstLatePaymentPenaltyCalculationData = d.copy(
    taxPeriodStartDate = LocalDate.of(2027, 4, 6),
    taxPeriodEndDate = LocalDate.of(2028, 4, 5),
    payPenaltyBy = LocalDate.of(2029, 4, 3),
    penaltyChargeCreationDate = Some(LocalDate.of(2029, 3, 2)),
    principalChargeDueDate = LocalDate.of(2029, 1, 31)
  )

  "FirstLatePaymentCalculationHelper.getPaymentDetails" should {
    "return 'Penalty paid' when penalty paid" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(
        llpHRCharge = Some(LLPCharge(2000.00, "30", 3.00)),
        isPenaltyPaid = true,
        isEstimate = false,
        incomeTaxIsPaid = false
      ))
      helper.getPaymentDetails(data) shouldBe Some("Penalty paid")
    }

    "return 'Pay penalty by {date}' when income tax is paid before day 31 and LLP1 becomes due" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(
        llpHRCharge = None,
        isEstimate = false,
        isPenaltyPaid = false,
        isPenaltyOverdue = false,
        incomeTaxIsPaid = true
      ))
      helper.getPaymentDetails(data) shouldBe Some("Pay penalty by " + DateFormatter.dateToString(data.payPenaltyBy))
    }

    "return None when the penalty is an estimate" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(llpHRCharge = None, incomeTaxIsPaid = false, isEstimate = true))
      helper.getPaymentDetails(data) shouldBe None
    }
  }

  "FirstLatePaymentCalculationHelper.getMissedDeadlineMsg" should {
    "return estimate message when no HR charge exists and tax is unpaid" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(llpHRCharge = None, incomeTaxIsPaid = false))
      helper.getMissedDeadlineMsg(data) shouldBe "Because you missed this deadline, you will be charged a late payment penalty."
    }

    "return paid message when paid" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(
        llpHRCharge = Some(LLPCharge(2000.00, "30", 3.00)),
        isPenaltyPaid = true,
        isEstimate = false,
        incomeTaxIsPaid = false
      ))
      helper.getMissedDeadlineMsg(data) shouldBe "Because you missed this deadline, you were charged a late payment penalty."
    }

    "return due/overdue message when overdue and not paid" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(
        llpHRCharge = Some(LLPCharge(2000.00, "30", 3.00)),
        isEstimate = false,
        isPenaltyPaid = false,
        isPenaltyOverdue = true,
        incomeTaxIsPaid = false
      ))
      helper.getMissedDeadlineMsg(data) shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
    }
  }

  "FirstLatePaymentCalculationHelper.getFinalUnpaidMsg" should {
    "return estimate + stop message when unpaid estimate and no payment plan (non-PFA)" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(llpHRCharge = None, incomeTaxIsPaid = false))
      val result = helper.getFinalUnpaidMsg(data, "")

      val isEstimateMsg = "This penalty is currently an estimate because the outstanding tax for the " + DateFormatter.dateToYearString(data.taxPeriodStartDate) + " to " + DateFormatter.dateToYearString(data.taxPeriodEndDate) + " tax year has not been paid."
      val stopMsg = "To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
      result shouldBe Some(isEstimateMsg + " " + stopMsg)
    }

    "return only estimate message when payment plan proposed" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData().copy(llpHRCharge = None, incomeTaxIsPaid = false, paymentPlanProposed = Some(LocalDate.of(2027, 6, 20))))
      val result = helper.getFinalUnpaidMsg(data, "")

      val isEstimateMsg = "This penalty is currently an estimate because the outstanding tax for the " + DateFormatter.dateToYearString(data.taxPeriodStartDate) + " to " + DateFormatter.dateToYearString(data.taxPeriodEndDate) + " tax year has not been paid."
      result shouldBe Some(isEstimateMsg)
    }

    "return None when income tax paid" in {
      val a = withTaxYear2027(sampleFirstLPPCalcData().copy(incomeTaxIsPaid = true))
      helper.getFinalUnpaidMsg(a, "") shouldBe None
    }

    "return None when llpHRCharge is present" in {
      val b = withTaxYear2027(sampleFirstLPPCalcData().copy(llpHRCharge = Some(LLPCharge(2000.00, "30", 3.00)), incomeTaxIsPaid = false))
      helper.getFinalUnpaidMsg(b, "") shouldBe None
    }
  }

  "FirstLatePaymentCalculationHelper.getPaymentPlanInset" should {
    "return inset when payment plan proposed" in {
      val proposed = withTaxYear2027(sampleFirstLPPCalcData().copy(paymentPlanProposed = Some(LocalDate.of(2027, 6, 20))))
      helper.getPaymentPlanInset(proposed) shouldBe Some("You proposed a payment plan on " + DateFormatter.dateToString(proposed.paymentPlanProposed.get) + ". If this payment plan is agreed your penalty will not increase.")
    }

    "return None when no proposed payment plan" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      helper.getPaymentPlanInset(data) shouldBe None
    }
  }

  "FirstLatePaymentCalculationHelper.getPaymentPlanHeading" should {
    "return heading when payment plan agreed" in {
      val agreed = withTaxYear2027(sampleFirstLPPCalcData().copy(paymentPlanAgreed = Some(LocalDate.of(2027, 6, 20))))
      helper.getPaymentPlanHeading(agreed) shouldBe Some("Your payment plan")
    }

    "return None when not agreed" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      helper.getPaymentPlanHeading(data) shouldBe None
    }
  }

  "FirstLatePaymentCalculationHelper.getPaymentPlanContent" should {
    "return content list when agreed" in {
      val agreed = withTaxYear2027(sampleFirstLPPCalcData().copy(paymentPlanAgreed = Some(LocalDate.of(2027, 6, 20))))
      val content = helper.getPaymentPlanContent(agreed)
      content.head shouldBe "You agreed to a payment plan on " + DateFormatter.dateToString(agreed.paymentPlanAgreed.get) + "."
      content(1) shouldBe "This penalty will not increase if you keep up with payments."
      content(2) shouldBe "If you do not, your payment plan will fail. Any penalties you owe will be calculated from their original date."
    }

    "return empty list when not agreed" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      helper.getPaymentPlanContent(data) shouldBe List.empty
    }
  }

  "FirstLatePaymentCalculationHelper.isExpiredBreathingSpace" should {
    val fixedNow: LocalDate = LocalDate.of(2029, 6, 10)
    val fixedTimeMachine: TimeMachine = new TimeMachine(app.injector.instanceOf[AppConfig]) {
      override def getCurrentDate(): LocalDate = fixedNow
    }

    "return false when no breathing space data" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      helper.isExpiredBreathingSpace(data, None, fixedTimeMachine) shouldBe false
    }

    "detect expired breathing space when end date before now and intersects principal date" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      val bs = BreathingSpace(
        bsStartDate = data.principalChargeDueDate.plusDays(1),
        bsEndDate = data.principalChargeDueDate.plusDays(10)
      )
      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe true
    }

    "detect expired breathing space when end date sits within the 31-day window" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      val bs = BreathingSpace(
        bsStartDate = data.principalChargeDueDate.minusDays(2),
        bsEndDate = data.principalChargeDueDate.plusDays(1)
      )
      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe true
    }

    "return false when breathing space ends in the future" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      val bs = BreathingSpace(bsStartDate = fixedNow.plusDays(1), bsEndDate = fixedNow.plusDays(5))
      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe false
    }

    "handle boundary: end date equal to principalChargeDueDate (not expired)" in {
      val data = withTaxYear2027(sampleFirstLPPCalcData())
      val bs = BreathingSpace(bsStartDate = data.principalChargeDueDate.minusDays(5), bsEndDate = data.principalChargeDueDate)
      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe false
    }
  }

}
