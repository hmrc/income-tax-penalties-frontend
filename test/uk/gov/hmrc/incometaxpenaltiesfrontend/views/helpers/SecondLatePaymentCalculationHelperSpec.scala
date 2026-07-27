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
      val agreedDate = LocalDate.of(2026, 6, 20)
      val agreed = withTaxYear2027(sampleSecondLPPCalcData().copy(paymentPlanAgreed = Some(agreedDate)))
      val content = helper.getPaymentPlanContent(agreed)
      content.head shouldBe "You agreed to a payment plan on " + DateFormatter.dateToString(agreedDate) + "."
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

  "SecondLatePaymentCalculationHelper.chargePeriods" should {
    val fixedNow: LocalDate = LocalDate.of(2027, 6, 10)
    val fixedTimeMachine: TimeMachine = new TimeMachine(app.injector.instanceOf[AppConfig]) {
      override def getCurrentDate(): LocalDate = fixedNow
    }

    // principalChargeDueDate + 31 = LPP2 start date
    val principalChargeDueDate: LocalDate = LocalDate.of(2027, 1, 1)
    val lpp2Start: LocalDate = principalChargeDueDate.plusDays(31) // 01 Feb 2027

    // Convenience builder: a Posted, unpaid LPP2 by default (calculation end date falls back to today).
    def calcData(incomeTaxPaidDate: Option[LocalDate] = None): SecondLatePaymentPenaltyCalculationData =
      sampleSecondLPPCalcData(isEstimate = false).copy(
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        principalChargeDueDate = principalChargeDueDate,
        incomeTaxPaidDate = incomeTaxPaidDate,
        penaltyChargeCreationDate = None
      )

    "return a single period from LPP2 start to today when there is no breathing space and tax is unpaid" in {
      helper.chargePeriods(calcData(), None, fixedTimeMachine) shouldBe Seq(lpp2Start -> fixedNow)
    }

    "return a single period from LPP2 start to the tax paid date when tax has been paid" in {
      val taxPaid = LocalDate.of(2027, 4, 30)
      helper.chargePeriods(calcData(Some(taxPaid)), None, fixedTimeMachine) shouldBe Seq(lpp2Start -> taxPaid)
    }

    "return Seq.empty when income tax was paid before LPP2 starts" in {
      val taxPaidBeforeLpp2 = lpp2Start.minusDays(10)
      helper.chargePeriods(calcData(Some(taxPaidBeforeLpp2)), None, fixedTimeMachine) shouldBe Seq.empty
    }

    "have no effect when a breathing space ended before LPP2 starts" in {
      val bs = BreathingSpace(bsStartDate = lpp2Start.minusDays(20), bsEndDate = lpp2Start.minusDays(5))
      helper.chargePeriods(calcData(), Some(Seq(bs)), fixedTimeMachine) shouldBe Seq(lpp2Start -> fixedNow)
    }

    "return two periods when LPP2 starts before a breathing space that has since ended" in {
      val bsStart = lpp2Start.plusDays(10)
      val bsEnd = lpp2Start.plusDays(20) // in the past relative to fixedNow
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.chargePeriods(calcData(), Some(Seq(bs)), fixedTimeMachine) shouldBe Seq(
        lpp2Start -> bsStart.minusDays(1),
        bsEnd.plusDays(1) -> fixedNow
      )
    }

    "return only the period before a breathing space that is still active today" in {
      val bsStart = lpp2Start.plusDays(10)
      val bsEnd = fixedNow.plusDays(5) // still active
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.chargePeriods(calcData(), Some(Seq(bs)), fixedTimeMachine) shouldBe Seq(
        lpp2Start -> bsStart.minusDays(1)
      )
    }

    "return only the period after a breathing space when LPP2 starts within an ended breathing space" in {
      val bsStart = lpp2Start.minusDays(5)
      val bsEnd = lpp2Start.plusDays(20) // ended, and covers LPP2 start
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.chargePeriods(calcData(), Some(Seq(bs)), fixedTimeMachine) shouldBe Seq(
        bsEnd.plusDays(1) -> fixedNow
      )
    }

    "not create a period after the breathing space when tax was paid during it" in {
      val bsStart = lpp2Start.plusDays(10)
      val bsEnd = lpp2Start.plusDays(30)
      val taxPaidDuringBs = lpp2Start.plusDays(20)
      val bs = BreathingSpace(bsStartDate = bsStart, bsEndDate = bsEnd)

      helper.chargePeriods(calcData(Some(taxPaidDuringBs)), Some(Seq(bs)), fixedTimeMachine) shouldBe Seq(
        lpp2Start -> bsStart.minusDays(1)
      )
    }

    "use penaltyChargeCreationDate as the end date for Posted penalties when the tax paid date is unavailable" in {
      val creationDate = LocalDate.of(2027, 3, 15)
      val data = calcData().copy(penaltyChargeCreationDate = Some(creationDate))
      helper.chargePeriods(data, None, fixedTimeMachine) shouldBe Seq(lpp2Start -> creationDate)
    }

    "process multiple breathing spaces, splitting the charge window around each ended one" in {
      val bs1 = BreathingSpace(bsStartDate = lpp2Start.plusDays(10), bsEndDate = lpp2Start.plusDays(20))
      val bs2 = BreathingSpace(bsStartDate = lpp2Start.plusDays(40), bsEndDate = lpp2Start.plusDays(50))

      helper.chargePeriods(calcData(), Some(Seq(bs2, bs1)), fixedTimeMachine) shouldBe Seq(
        lpp2Start -> lpp2Start.plusDays(9),
        lpp2Start.plusDays(21) -> lpp2Start.plusDays(39),
        lpp2Start.plusDays(51) -> fixedNow
      )
    }
  }

}
