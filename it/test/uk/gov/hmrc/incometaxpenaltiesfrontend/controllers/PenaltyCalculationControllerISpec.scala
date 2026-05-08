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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import fixtures.PenaltiesDetailsTestData
import org.jsoup.Jsoup
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.*

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PenaltyCalculationControllerISpec extends ControllerISpecHelper
  with PenaltiesStub with PenaltiesDetailsTestData with FeatureSwitching {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  def addQueryParam(pathNoQuery: String): String = {
    pathNoQuery + "?penaltyId=" + principleChargeRef
  }

  def getDateString(date: LocalDate): String = s"${date.getDayOfMonth} ${messagesAPI(s"month.${date.getMonthValue}")} ${date.getYear}"
  def getTaxYearString(calcData: CalculationData): String = calcData.taxPeriodStartDate.getYear.toString + " to " + calcData.taxPeriodEndDate.getYear.toString

  List(false, true).foreach { isAgent =>
    val pathStart = if (isAgent) "/agent-" else "/"
    val firstLPPPath = addQueryParam(pathStart + "first-lpp-calculation")
    val secondLPPPath = addQueryParam(pathStart + "second-lpp-calculation")
    val optArn = if(isAgent) Some("123456789") else None

    val yourPenaltyDetails = "Your penalty details"
    s"GET $firstLPPPath" should {
      setFeatureDate(None)

      "render the expected first late payment calculation" when {
        "a first late payment penalty exists for the penaltyId" that {
          
          "has back link" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getElementsByClass("govuk-back-link").text() shouldBe "Back"
          }

          //scenario 1
          "is between 15 and 30 days and the tax is unpaid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you will be charged a late payment penalty."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(firstLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }

          //scenario 2
          "is between 15 and 30 days and the tax is paid but not penalty" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(isIncomeTaxPaid = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }

          //scenario 3
          "is between 15 and 30 days and the tax paid late and penalty is not paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(isIncomeTaxPaid = true,
              isOverdue = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }


          //scenario 4
          "is over 30 days, tax has not been paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }

          //scenario 5
          "is over 30 days, the tax is paid but not penalty and is overdue" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false, isIncomeTaxPaid = true, isOverdue = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }


          //scenario 6
          "is between 15 and 30 days and the tax and penalty is paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(isIncomeTaxPaid = true, isPenaltyPaid = true, isEstimate = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }

          //scenario 7
          "is over 30 days and the tax and penalty is paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false, isIncomeTaxPaid = true, isPenaltyPaid = true, isEstimate = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
            document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
            document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
            document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }

          //scenario 8
          "is between 15 and 30 days and the tax is unpaid with breathing space" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPageWithBreathingSpace(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("breathingSpaceMessage").text() shouldBe "You are in Breathing Space. This penalty is paused and will not increase. The time you are in Breathing Space will not be added to your calculation."
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you will be charged a late payment penalty."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(firstLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }


          //scenario 9
          "is between 15 and 30 days and the tax paid late and penalty is not paid and isPFA" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(isIncomeTaxPaid = true,
              isOverdue = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPagePFA(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("taxYearAmended").text() shouldBe s"Your tax return for the ${getTaxYearString(firstLPPCalcData)} tax year has been amended."
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the extra amount was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }


          //scenario 10
          "is over 30 days, tax has not been paid and isPFA" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPagePFA(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("taxYearAmended").text() shouldBe s"Your tax return for the ${getTaxYearString(firstLPPCalcData)} tax year has been amended."
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the extra amount was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }


          //scenario 11
          "is between 15 and 30 days and the tax is unpaid with ex breathing space" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPageWithExBreathingSpace(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.principalChargeDueDate)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you will be charged a late payment penalty."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(firstLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
            document.getElementById("breathingSpaceExpired").text() shouldBe "Your Breathing Space has ended. The time you were in Breathing Space has not been added to your calculation."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }

        }
      }

      "a penalty does not exist for the penaltyId" should {
        "redirect to penalties home" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(emptyPenaltyDetailsModel))

          val result = get(firstLPPPath, isAgent)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.IndexController.homePage(isAgent).url)
        }
      }
    }

    s"GET $secondLPPPath" should {

      "render the expected second late payment calculation" when {
        "a second late payment penalty exists for the penaltyId" that {

          "has back link" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getElementsByClass("govuk-back-link").text() shouldBe "Back"
          }

          //scenario 1
          "the tax is unpaid" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you will be charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty will increase daily at an annual rate of 10% of the outstanding tax."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(secondLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }

          //scenario 2
          "tax is paid" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData(isIncomeTaxPaid = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(secondLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"


          }

          //scenario 3
          "tax is paid but penalty overdue accruing interest" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData(isIncomeTaxPaid = true,
              isOverdue = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)
            val chargePeriod = "Charge period"
            val estimatedPenalty = "Estimated penalty"

            def getPenaltyAmount(calcData: CalculationData): String = calcData.formattedPenaltyAmount
            val estimatedPenaltyAmount = getPenaltyAmount(secondLPPCalcData)
            //endDateChargePeriod when it has been estimated:
            def endDateChargePeriod = getFeatureDate(appConfig)
            val startDateChargePeriod = secondLPPCalcData.payPenaltyBy.plusDays(31)
            def getEndDateChargePeriod = getDateString(endDateChargePeriod)
            def getStartDateChargePeriod = getDateString(startDateChargePeriod)
            def getChargePeriodDays = ChronoUnit.DAYS.between(startDateChargePeriod, endDateChargePeriod).toInt + 1

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(secondLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
            document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
            document.getElementById("penaltyAmountDetailsP1").text() shouldBe "We use a 10% yearly rate. This means the amount increases a small amount every day."
            document.getElementById("penaltyAmountDetailsP2").text() shouldBe "Each day, we:"
            document.getElementById("penaltyAmountDetailsPoint1").text() shouldBe "take the unpaid income tax"
            document.getElementById("penaltyAmountDetailsPoint2").text() shouldBe "multiply by 0.10 (the annual rate)"
            document.getElementById("penaltyAmountDetailsPoint3").text() shouldBe "divide the amount by days in a year"
            document.getElementById("penaltyAmountDetailsPoint4").text() shouldBe "add all the daily amounts together to get the total amount"
            document.select("#second-lpp-penalty-details-table tr:nth-child(1) td:nth-child(1)").first().text() shouldBe s"$chargePeriod"
            document.select("#second-lpp-penalty-details-table tr:nth-child(1) td:nth-child(2)").first().text() shouldBe s"$getStartDateChargePeriod to $getEndDateChargePeriod ($getChargePeriodDays days)"
            document.select("#second-lpp-penalty-details-table tr:nth-child(2) td:nth-child(1)").first().text() shouldBe s"$estimatedPenalty"
            document.select("#second-lpp-penalty-details-table tr:nth-child(2) td:nth-child(2)").first().text() shouldBe s"£$estimatedPenaltyAmount"
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }

          //scenario 4
          "penalty paid" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData(isIncomeTaxPaid = true, isPenaltyPaid = true, isEstimate = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(secondLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }


          //scenario 5
          "the tax is unpaid and breathing space is true" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPageWithBreathingSpace(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)
            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("breathingSpaceMessage").text() shouldBe "You are in Breathing Space. This penalty is paused and will not increase. The time you are in Breathing Space will not be added to your calculation."
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you will be charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty will increase daily at an annual rate of 10% of the outstanding tax."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(secondLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }


          //scenario 6
          "the tax is unpaid and breathing space has expired" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPageWithExBreathingSpace(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)
            document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
            document.getElementById("breathingSpaceExpired").text() shouldBe "Your Breathing Space has ended. The time you were in Breathing Space has not been added to your calculation."
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.principalChargeDueDate)}."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you will be charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty will increase daily at an annual rate of 10% of the outstanding tax."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(secondLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
            document.getElementById("penaltyDetailsHeading").text() shouldBe s"$yourPenaltyDetails"

          }

        }
      }

      "a penalty does not exist for the penaltyId" should {
        "redirect to penalties home" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(emptyPenaltyDetailsModel))

          val result = get(secondLPPPath, isAgent)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.IndexController.homePage(isAgent).url)
        }
      }
    }
  }
}
