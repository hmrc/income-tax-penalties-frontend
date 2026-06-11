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
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter
import java.time.LocalDate
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.SecondLatePaymentPenaltyCalculationData
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.breathingSpace.BreathingSpace
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyStatusEnum

class SecondLatePaymentCalculationHelperSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite
  with PenaltiesDetailsTestData {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))

  val helper = new SecondLatePaymentCalculationHelper

  private def withTaxYear2027(d: SecondLatePaymentPenaltyCalculationData): SecondLatePaymentPenaltyCalculationData = d.copy(
    taxPeriodStartDate = LocalDate.of(2027, 4, 6),
    taxPeriodEndDate = LocalDate.of(2028, 4, 5),
    payPenaltyBy = LocalDate.of(2029, 4, 3),
    penaltyChargeCreationDate = Some(LocalDate.of(2029, 3, 2)),
    chargeStartDate = Some(LocalDate.of(2029, 3, 2)),
    chargeEndDate = LocalDate.of(2029, 3, 2),
    principalChargeDueDate = LocalDate.of(2029, 1, 31)
  )


  "SecondLatePaymentCalculationHelper.getPaymentDetails" should {

    "return 'Penalty paid' when penalty paid" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData(isPenaltyPaid = true, isIncomeTaxPaid = true, isEstimate = false))
      helper.getPaymentDetails(data) shouldBe Some("Penalty paid")
    }

    "return 'Pay penalty by {date}' when income tax paid and not estimate" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData(isIncomeTaxPaid = true, isEstimate = false))
      helper.getPaymentDetails(data) shouldBe Some("Pay penalty by " + DateFormatter.dateToString(data.payPenaltyBy))
    }

    "return None when estimate and income tax unpaid" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData())
      helper.getPaymentDetails(data) shouldBe None
    }
  }

  "SecondLatePaymentCalculationHelper.getMissedDeadlineAndDailyIncreaseMsgs" should {
    "return estimate messages when calculation is an estimate and income tax is unpaid" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData())
      val result = helper.getMissedDeadlineAndDailyIncreaseMsgs(data)

      result._1 shouldBe "Because you missed this deadline by more than 30 days, you will be charged a second late payment penalty."
      result._2 shouldBe "This penalty will increase daily at an annual rate of 10% of the outstanding tax."
    }

    "return paid messages when the penalty has been paid" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData(isPenaltyPaid = true, isIncomeTaxPaid = true, isEstimate = false))
      val result = helper.getMissedDeadlineAndDailyIncreaseMsgs(data)

      result._1 shouldBe "Because you missed this deadline by more than 30 days, you were charged a second late payment penalty."
      result._2 shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
    }

    "return due/overdue messages for other cases - Due/Overdue" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData(isEstimate = false))
      val result = helper.getMissedDeadlineAndDailyIncreaseMsgs(data)

      result._1 shouldBe "Because you missed this deadline by more than 30 days, you have been charged a second late payment penalty."
      result._2 shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
    }
  }

  "SecondLatePaymentCalculationHelper.getFinalUnpaidMsg" should {
    "return estimate + stop message when unpaid estimate and no payment plan" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData())
      val result = helper.getFinalUnpaidMsg(data)

      val isEstimateMsg = "This penalty is currently an estimate because the outstanding tax for the " + DateFormatter.dateToYearString(data.taxPeriodStartDate) + " to " + DateFormatter.dateToYearString(data.taxPeriodEndDate) + " tax year has not been paid."
      val stopMsg = "To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."

      result shouldBe Some(isEstimateMsg + " " + stopMsg)
    }

    "return only estimate message when payment plan proposed/agreed" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData().copy(paymentPlanProposed = Some(LocalDate.of(2026, 6, 20))))
      val result = helper.getFinalUnpaidMsg(data)

      val isEstimateMsg = "This penalty is currently an estimate because the outstanding tax for the " + DateFormatter.dateToYearString(data.taxPeriodStartDate) + " to " + DateFormatter.dateToYearString(data.taxPeriodEndDate) + " tax year has not been paid."

      result shouldBe Some(isEstimateMsg)
    }

    "return None when not estimate or income tax paid" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData(isEstimate = false, isIncomeTaxPaid = true))
      helper.getFinalUnpaidMsg(data) shouldBe None
    }
  }

  "SecondLatePaymentCalculationHelper.getPaymentPlanInset" should {
    "return inset when payment plan proposed" in {
      val proposed = withTaxYear2027(sampleSecondLPPCalcData().copy(paymentPlanProposed = Some(LocalDate.of(2026, 6, 20))))
      helper.getPaymentPlanInset(proposed) shouldBe Some("You proposed a payment plan on " + DateFormatter.dateToString(proposed.paymentPlanProposed.get) + ". If this payment plan is agreed your penalty will not increase.")
    }

    "return None when no proposed payment plan" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData())
      helper.getPaymentPlanInset(data) shouldBe None
    }
  }

  "SecondLatePaymentCalculationHelper.getPaymentPlanHeading" should {
    "return heading when payment plan agreed" in {
      val agreed = withTaxYear2027(sampleSecondLPPCalcData().copy(paymentPlanAgreed = Some(LocalDate.of(2026, 6, 20))))
      helper.getPaymentPlanHeading(agreed) shouldBe Some("Your payment plan")
    }

    "return None when not agreed" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData())
      helper.getPaymentPlanHeading(data) shouldBe None
    }
  }

  "SecondLatePaymentCalculationHelper.getPaymentPlanContent" should {
    "return content list when agreed" in {
      val agreed = withTaxYear2027(sampleSecondLPPCalcData().copy(paymentPlanAgreed = Some(LocalDate.of(2026, 6, 20))))
      val content = helper.getPaymentPlanContent(agreed)
      content.head shouldBe "You agreed to a payment plan on " + DateFormatter.dateToString(agreed.paymentPlanAgreed.get) + "."
      content(1) shouldBe "This calculation is an estimate up to when you agreed your payment plan. It will remain an estimate until your payment plan pays off the outstanding tax."
      content(2) shouldBe "You must keep up with payments. If you do not, your payment plan will fail. Any penalties you owe will be calculated from their original date."
    }

    "return empty list when not agreed" in {
      val data = withTaxYear2027(sampleSecondLPPCalcData())
      helper.getPaymentPlanContent(data) shouldBe List.empty
    }
  }

  "SecondLatePaymentCalculationHelper.isExpiredBreathingSpace" should {
    val fixedNow: LocalDate = LocalDate.of(2027, 6, 10)
    val fixedTimeMachine: TimeMachine = new TimeMachine(app.injector.instanceOf[AppConfig]) {
      override def getCurrentDate(): LocalDate = fixedNow
    }

    "return false when no breathing space data" in {
      val data = sampleSecondLPPCalcData().copy(
        principalChargeDueDate = fixedNow.minusDays(60),
        penaltyChargeCreationDate = Some(fixedNow.minusDays(30))
      )

      helper.isExpiredBreathingSpace(data, None, fixedTimeMachine) shouldBe false
    }

    "detect expired breathing space (generic case)" in {
      val base = sampleSecondLPPCalcData()
      val data = base.copy(
        principalChargeDueDate = fixedNow.minusDays(60),
        penaltyChargeCreationDate = Some(fixedNow.minusDays(30))
      )

      val bsStart = fixedNow.minusDays(30)
      val bsEnd = fixedNow.minusDays(23)
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe true
    }

    "breathing space: accruing branch true when end date after principal+30 and before now" in {
      val base = sampleSecondLPPCalcData()
      val data = base.copy(
        principalChargeDueDate = fixedNow.minusDays(40),
        penaltyChargeCreationDate = Some(fixedNow.minusDays(10)),
        penaltyStatus = LPPPenaltyStatusEnum.Accruing
      )

      val bsEnd = data.principalChargeDueDate.plusDays(31)
      val bsStart = data.principalChargeDueDate.plusDays(10)
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe true
    }

    "breathing space: posted branch case3 true when spans penalty creation date" in {
      val base = sampleSecondLPPCalcData()
      val data = base.copy(
        principalChargeDueDate = fixedNow.minusDays(50),
        penaltyChargeCreationDate = Some(fixedNow.minusDays(10))
      )

      val bsStart = data.principalChargeDueDate.plusDays(1)
      val bsEnd = data.penaltyChargeCreationDate.get.plusDays(5)
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe true
    }

    "breathing space: posted branch false when end date before principal+31" in {
      val base = sampleSecondLPPCalcData()
      val data = base.copy(
        principalChargeDueDate = fixedNow.minusDays(60),
        penaltyChargeCreationDate = Some(fixedNow.minusDays(30))
      )

      val bsStart = data.principalChargeDueDate.plusDays(1)
      val bsEnd = data.principalChargeDueDate.plusDays(10)
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe false
    }

    "breathing space: false when end date is in the future" in {
      val base = sampleSecondLPPCalcData()
      val data = base.copy(
        principalChargeDueDate = fixedNow.minusDays(60),
        penaltyChargeCreationDate = Some(fixedNow.minusDays(30))
      )

      val bsStart = data.principalChargeDueDate.plusDays(31)
      val bsEnd = fixedNow.plusDays(5)
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.isExpiredBreathingSpace(data, Some(Seq(bs)), fixedTimeMachine) shouldBe false
    }
  }

}
