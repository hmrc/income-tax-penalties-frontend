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
import uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels.CalculationData

import java.time.LocalDate

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

    val youOrClient = if(isAgent){"your client"}else{"you"}
    val youOrThey = if(isAgent){"they"}else{"you"}
    val youOrTheyCaps = if(isAgent){"They"}else{"You"}
    val youHaveOrThey = if(isAgent){"They"}else{"You have"}
    val youOrYourClientShould = if(isAgent){"your client should"}else{"please"}

    s"GET $firstLPPPath" should {

      "render the expected first late payment calculation" when {
        "a first late payment penalty exists for the penaltyId" that {

          //scenario 1
          "is between 15 and 30 days and the tax is unpaid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline, $youOrThey will be charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe s"$youHaveOrThey missed the deadline by 15-30 days, so $youOrThey will be charged 2% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe s"If $youOrThey miss the deadline by more than 30 days, this penalty will increase by an additional 2% of the tax that is outstanding 30 days after the payment deadline"
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(firstLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, $youOrYourClientShould pay the outstanding tax immediately or set up a payment plan."
          }

          //scenario 2
          "is between 15 and 30 days and the tax is paid but not penalty" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(isIncomeTaxPaid = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline, $youOrThey have been charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").size() shouldBe 1
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe s"$youOrTheyCaps missed the deadline by 15-30 days, so $youOrThey have been charged 2% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            document.getElementById("penaltyStatus").text() shouldBe s"To avoid interest charges, $youOrClient should pay this penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}."

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

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline, $youOrThey have been charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").size() shouldBe 1
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe s"$youOrTheyCaps missed the deadline by 15-30 days, so $youOrThey have been charged 2% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is now overdue and interest is being charged."

          }


          //scenario 4
          "is over 30 days, tax has not been paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "2% of £99.99 (the tax that was outstanding 15 days after the payment deadline)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "An additional 2% of £99.99 (the tax that was outstanding 30 days after the payment deadline)"
            document.getElementById("penaltyStatus").text() shouldBe s"To avoid interest charges, $youOrClient should pay this penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}."


          }

          //scenario 5
          "is over 30 days, the tax is paid but not penalty and is overdue" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false, isIncomeTaxPaid = true, isOverdue = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "2% of £99.99 (the tax that was outstanding 15 days after the payment deadline)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "An additional 2% of £99.99 (the tax that was outstanding 30 days after the payment deadline)"
            document.getElementById("penaltyStatus").text() shouldBe "This penalty is now overdue and interest is being charged."


          }


          //scenario 6
          "is between 15 and 30 days and the tax and penalty is paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(isIncomeTaxPaid = true, isPenaltyPaid = true, isEstimate = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline, $youOrThey have been charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").size() shouldBe 1
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe s"$youOrTheyCaps missed the deadline by 15-30 days, so $youOrThey have been charged 2% of the tax that was outstanding 15 days after the payment deadline (£99.99)"


          }

          //scenario 7
          "is over 30 days and the tax and penalty is paid" in {
            stubAuthRequests(isAgent)
            val firstLPPCalcData = sampleFirstLPPCalcData(is15to30Days = false, isIncomeTaxPaid = true, isPenaltyPaid = true, isEstimate = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(firstLPPCalcData)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(firstLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(firstLPPCalcData)} tax year was ${getDateString(firstLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "2% of £99.99 (the tax that was outstanding 15 days after the payment deadline)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "An additional 2% of £99.99 (the tax that was outstanding 30 days after the payment deadline)"


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

          //scenario 1
          "the tax is unpaid" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData()
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey will be charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty will increase daily at an annual rate of 10% of the outstanding tax."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is currently an estimate because the outstanding tax for the ${getTaxYearString(secondLPPCalcData)} tax year has not been paid. To stop this estimated penalty increasing further, $youOrYourClientShould pay the outstanding tax immediately or set up a payment plan."
          }

          //scenario 2
          "tax is paid" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData(isIncomeTaxPaid = true)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(secondLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey have been charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
            document.getElementById("penaltyStatus").text() shouldBe s"To avoid interest charges, $youOrThey should pay this penalty by ${getDateString(secondLPPCalcData.payPenaltyBy)}."

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

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Pay penalty by ${getDateString(secondLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey have been charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."
            document.getElementById("penaltyStatus").text() shouldBe s"This penalty is now overdue and interest is being charged."

          }

          //scenario 4
          "penalty paid" in {
            stubAuthRequests(isAgent)
            val secondLPPCalcData = sampleSecondLPPCalcData(isIncomeTaxPaid = true, isPenaltyPaid = true, isEstimate = false)
            stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPage(secondLPPCalcData)))
            val result = get(secondLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe s"Penalty paid on ${getDateString(secondLPPCalcData.payPenaltyBy)}"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe s"The payment deadline for the ${getTaxYearString(secondLPPCalcData)} tax year was ${getDateString(secondLPPCalcData.payPenaltyBy)}."
            document.getElementById("missedDeadline").text() shouldBe s"Because $youOrClient missed this deadline by more than 30 days, $youOrThey have been charged a second late payment penalty."
            document.getElementById("penaltyIncrease").text() shouldBe "This penalty increased daily at an annual rate of 10% until the outstanding tax was paid."

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