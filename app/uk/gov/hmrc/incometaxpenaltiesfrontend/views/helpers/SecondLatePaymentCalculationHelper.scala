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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.breathingSpace.BreathingSpace
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LPPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.{DateFormatter, TimeMachine}
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.DateFormatter.{dateToString, dateToYearString}
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.SecondLatePaymentPenaltyCalculationData

import java.time.LocalDate

class SecondLatePaymentCalculationHelper {

  /**
   * Works out the charge period(s) over which the LPP2 penalty accrues, splitting the period around any
   * breathing space window(s).
   *
   * LPP2_START          = principal charge due date + 31 days
   * CalculationEndDate  = the date income tax was paid; otherwise (for Posted penalties) the penalty
   *                       charge creation date; otherwise today.
   *
   * The charge window `[LPP2_START, CalculationEndDate]` is suspended for the duration of each breathing
   * space. An ended breathing space splits the window into a "before" and an "after" part; a breathing
   * space that is still active today truncates the window at the day before it starts (there is no
   * "after" part yet). Only chronologically valid periods (start on or before end) are returned, so an
   * empty result means there is nothing to charge (e.g. income tax was paid before LPP2 even started).
   */
  def chargePeriods(calculationData: SecondLatePaymentPenaltyCalculationData,
                    breathingSpaceData: Option[Seq[BreathingSpace]],
                    timeMachine: TimeMachine): Seq[(LocalDate, LocalDate)] = {

    val today: LocalDate = timeMachine.getCurrentDate()
    val lpp2Start: LocalDate = calculationData.principalChargeDueDate.plusDays(31)

    val earliestTtpDateOpt: Option[LocalDate] = (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(a), Some(b)) => Some(if (a.isBefore(b)) a else b)
      case (Some(a), None)    => Some(a)
      case (None, Some(b))    => Some(b)
      case _                  => None
    }

    val calculationEndDate: LocalDate =
      calculationData.incomeTaxPaidDate
        .orElse(earliestTtpDateOpt)
        .orElse(if (calculationData.penaltyStatus == LPPPenaltyStatusEnum.Posted) calculationData.penaltyChargeCreationDate else None)
        .getOrElse(today)

    def earlier(a: LocalDate, b: LocalDate): LocalDate = if (a.isBefore(b)) a else b
    def later(a: LocalDate, b: LocalDate): LocalDate = if (a.isAfter(b)) a else b

    // Only return a period when it is chronologically valid (start on or before end).
    def validPeriod(start: LocalDate, end: LocalDate): Seq[(LocalDate, LocalDate)] =
      if (!start.isAfter(end)) Seq(start -> end) else Seq.empty

    // Suspends charging for a single breathing space, potentially splitting the period into a
    // "before" part and an "after" part. While a breathing space is still active today there is no
    // "after" part, so charging is suspended all the way to the end of the window.
    def removeBreathingSpace(period: (LocalDate, LocalDate), bs: BreathingSpace): Seq[(LocalDate, LocalDate)] = {
      val (start, end) = period
      val suspendedUntil = if (bs.bsEndDate.isBefore(today)) bs.bsEndDate else end
      validPeriod(start, earlier(bs.bsStartDate.minusDays(1), end)) ++
        validPeriod(later(suspendedUntil.plusDays(1), start), end)
    }

    // Nothing to charge if the window is invalid (e.g. income tax was paid before LPP2 started).
    if (lpp2Start.isAfter(calculationEndDate)) {
      Seq.empty
    } else {
      // The model allows multiple breathing spaces, so each one is removed from the charge window in turn.
      breathingSpaceData.getOrElse(Seq.empty).sortBy(_.bsStartDate)
        .foldLeft(Seq(lpp2Start -> calculationEndDate)) { (periods, bs) =>
          periods.flatMap(removeBreathingSpace(_, bs))
        }
    }
  }


  def getPaymentDetails(calculationData: SecondLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {

    if (calculationData.isPenaltyPaid && !calculationData.isPenaltyOverdue && !calculationData.isEstimate) {
      Some(messages("calculation.paid.penalty"))
    } else if(!calculationData.isEstimate && !calculationData.isPenaltyPaid) {
      Some(messages("calculation.pay.penalty.by", DateFormatter.dateToString(calculationData.payPenaltyBy)))
    } else {
      None
    }
  }

  def LPP2CrystallisedMsg(calculationData: SecondLatePaymentPenaltyCalculationData, timeMachine: TimeMachine)(implicit messages: Messages): Option[String] = {
    val daysSinceDue = java.time.temporal.ChronoUnit.DAYS.between(calculationData.principalChargeDueDate, timeMachine.getCurrentDate())
    val hasPaymentPlan = calculationData.paymentPlanAgreed.isDefined || calculationData.paymentPlanProposed.isDefined

    if (calculationData.isEstimate && hasPaymentPlan && daysSinceDue >= 726) {
      Some(messages("calculation.missedDeadline.lpp2.726.message"))
    } else {
      None
    }
  }
  
  def getMissedDeadlineAndDailyIncreaseMsgs(calculationData: SecondLatePaymentPenaltyCalculationData, timeMachine: TimeMachine)(implicit messages: Messages): (String, Option[String], Option[String]) = {
    val hasTimeToPayArrangement = calculationData.paymentPlanAgreed.isDefined || calculationData.paymentPlanProposed.isDefined
    val dailyIncreaseMsg = if (hasTimeToPayArrangement) None else {
      Some(
        if (calculationData.isEstimate && !calculationData.incomeTaxIsPaid) {
          messages("calculation.dailyIncrease.lpp2.isEstimate")
        } else {
          messages("calculation.dailyIncrease.lpp2.isDueOrOverdueOrPaid")
        }
      )
    }

    if (calculationData.isEstimate && !calculationData.incomeTaxIsPaid) {
      (messages("calculation.missedDeadline.lpp2.isEstimate"), dailyIncreaseMsg, LPP2CrystallisedMsg(calculationData, timeMachine))
    } else if (calculationData.isPenaltyPaid) {
      (messages("calculation.missedDeadline.lpp2.isPaid"), dailyIncreaseMsg, None)
    } else {
      (messages("calculation.missedDeadline.lpp2.isDueOrOverdue"), dailyIncreaseMsg, None)
    }
  }


  def getFinalUnpaidMsg(calculationData: SecondLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {
    if (!calculationData.incomeTaxIsPaid && calculationData.isEstimate) {
      val isEstimateMsg = messages("calculation.calc2.penalty.isEstimate",
        dateToYearString(calculationData.taxPeriodStartDate),
        dateToYearString(calculationData.taxPeriodEndDate))
      val toStopEstimateIncMsg = messages("calculation.calc2.penalty.stopEstimateIncreasing")
      if (calculationData.paymentPlanAgreed.isDefined || calculationData.paymentPlanProposed.isDefined) {
        Some(isEstimateMsg)
      } else {
        Some(isEstimateMsg + " " + toStopEstimateIncMsg)
      }
    } else {
      None
    }
  }

  def getPaymentPlanInset(calculationData: SecondLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (None, Some(proposedDate)) =>
        Some(messages("calculation.calc2.penalty.payment.plan.proposed.inset", dateToString(proposedDate)))
      case _ => None
    }
  }

  def getPaymentPlanHeading(calculationData: SecondLatePaymentPenaltyCalculationData)(implicit messages: Messages): Option[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(_), _) =>
        Some(messages("calculation.calc2.penalty.payment.plan.agreed.h1"))
      case _ => None
    }
  }

  def getPaymentPlanContent(calculationData: SecondLatePaymentPenaltyCalculationData)(implicit messages: Messages): List[String] = {
    (calculationData.paymentPlanAgreed, calculationData.paymentPlanProposed) match {
      case (Some(agreedDate), _) =>
        List(
          messages("calculation.calc2.penalty.payment.plan.agreed.p1", dateToString(agreedDate)),
          messages("calculation.calc2.penalty.payment.plan.agreed.p2"),
          messages("calculation.calc2.penalty.payment.plan.agreed.bullet1"),
          messages("calculation.calc2.penalty.payment.plan.agreed.bullet2")
        )
      case _ => List.empty
    }
  }

  def isExpiredBreathingSpace(calculationData: SecondLatePaymentPenaltyCalculationData,
                              breathingSpaceData: Option[Seq[BreathingSpace]],
                              timeMachine: TimeMachine): Boolean = {
    breathingSpaceData match {
      case Some(breathingSpace) => breathingSpace.count(bs =>
        (bs.bsEndDate.isBefore(timeMachine.getCurrentDate()) && !bs.bsEndDate.isBefore(calculationData.principalChargeDueDate.plusDays(31))) &&
          (
            (calculationData.penaltyStatus == LPPPenaltyStatusEnum.Accruing && bs.bsEndDate.isAfter(calculationData.principalChargeDueDate.plusDays(30))) ||
              (calculationData.penaltyStatus == LPPPenaltyStatusEnum.Posted &&
                (
                  (bs.bsStartDate.isAfter(calculationData.principalChargeDueDate.plusDays(30)) && bs.bsStartDate.isBefore(calculationData.penaltyChargeCreationDate.get.plusDays(1))) ||
                    (bs.bsEndDate.isAfter(calculationData.principalChargeDueDate.plusDays(30)) && bs.bsEndDate.isBefore(calculationData.penaltyChargeCreationDate.get.plusDays(1))) ||
                    (bs.bsStartDate.isBefore(calculationData.principalChargeDueDate.plusDays(31)) && bs.bsEndDate.isAfter(calculationData.penaltyChargeCreationDate.get))
                  )
                )
            )
      ) > 0
      case None => false
    }
  }
}
