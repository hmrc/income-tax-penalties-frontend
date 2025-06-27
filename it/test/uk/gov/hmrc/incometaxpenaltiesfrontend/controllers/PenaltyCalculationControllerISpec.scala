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
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub

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

  List(false, true).foreach { isAgent =>
    val pathStart = if (isAgent) "/agent-" else "/"
    val firstLPPPath = addQueryParam(pathStart + "first-lpp-calculation")
    val secondLPPPath = addQueryParam(pathStart + "second-lpp-calculation")
    val optArn = if(isAgent) Some("123456789") else None
    s"GET $firstLPPPath" should {

      "render the expected first late payment calculation" when {
        "a first late payment penalty exists for the penaltyId" that {

          //scenario 1
          "is between 15 and 30 days and the tax is unpaid" in {
            stubAuthRequests(isAgent)
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(sampleUnpaidLPP1Day15to30)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you will be charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "You have missed the deadline by 15-30 days, so you will be charged 3% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "If you miss the deadline by more than 30 days, this penalty will increase by an additional 3% of the tax that is outstanding 30 days after the payment deadline"
            document.getElementById("penaltyStatus").text() shouldBe "This penalty is currently an estimate because the outstanding tax for the 2021 to 2021 tax year has not been paid. To stop this estimated penalty increasing further, please pay the outstanding tax immediately or set up a payment plan."
          }

          //scenario 2
          "is between 15 and 30 days and the tax is paid but not penalty" in {
            stubAuthRequests(isAgent)
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(sampleTaxPaidLPP1Day15to30)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe "Pay penalty by 8 July 2021"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "You missed the deadline by 15-30 days, so you have been charged 3% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            document.getElementById("penaltyStatus").text() shouldBe "To avoid interest charges, you should pay this penalty by 8 July 2021."

          }

          //scenario 3
          "is between 15 and 30 days and the tax and penalty is paid" in {
            stubAuthRequests(isAgent)
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(samplePaidLPP1Day15to30)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe "Pay penalty by 8 July 2021"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "You missed the deadline by 15-30 days, so you have been charged 3% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            //this message is incorrect. it should be "This penalty is now overdue and interest is being charged"
            document.getElementById("penaltyStatus").text() shouldBe "To avoid interest charges, you should pay this penalty by 8 July 2021."

          }

          //scenario 4
          "is over 30 days, tax has not been paid" in {
            stubAuthRequests(isAgent)
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(sampleUnpaidLPP1Day31)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1001.45"
            document.getElementById("payPenaltyBy").text() shouldBe "Pay penalty by 8 July 2021"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "3% of £99.99 (the tax that was outstanding 15 days after the payment deadline)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "An additional 3% of £99.99 (the tax that was outstanding 30 days after the payment deadline)"
            document.getElementById("penaltyStatus").text() shouldBe "To avoid interest charges, you should pay this penalty by 8 July 2021."


          }

          //scenario 5
          "is over 30 days, the tax is paid but not penalty" in {
            stubAuthRequests(isAgent)
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(sampleTaxPaidLPP1Day31)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £0"
            document.getElementById("payPenaltyBy").text() shouldBe "Pay penalty by 8 July 2021"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "3% of £99.99 (the tax that was outstanding 15 days after the payment deadline)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "An additional 3% of £99.99 (the tax that was outstanding 30 days after the payment deadline)"
            //this message is incorrect. it should be "This penalty is now overdue and interest is being charged"
            document.getElementById("penaltyStatus").text() shouldBe "To avoid interest charges, you should pay this penalty by 8 July 2021."


          }

          //scenario 6
          "is 15-30 days and the penalty is paid" in {
            stubAuthRequests(isAgent)
            //to update
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(samplePaidLPP1Day31)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £0"
            document.getElementById("payPenaltyBy").text() shouldBe "Pay penalty by 8 July 2021"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline, you have been charged a late payment penalty."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "You missed the deadline by 15-30 days, so you have been charged 3% of the tax that was outstanding 15 days after the payment deadline (£99.99)"
            //this message is incorrect. it should be empty
            document.getElementById("penaltyStatus") shouldBe null

            //scenario 7
          "is over 30 days and the tax and penalty is paid" in {
            stubAuthRequests(isAgent)
            stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(getPenaltyDetailsForCalculationPage(samplePaidLPP1Day31)))
            val result = get(firstLPPPath, isAgent)
            result.status shouldBe OK

            val document = Jsoup.parse(result.body)

            document.getServiceName.text() shouldBe "Manage your Self Assessment"
            document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
            document.getH1Elements.text() shouldBe "First late payment penalty calculation"
            document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £0"
            document.getElementById("payPenaltyBy").text() shouldBe "Pay penalty by 8 July 2021"
            document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
            document.getElementById("paymentDeadline").text() shouldBe "The payment deadline for the 2021 to 2021 tax year was 8 July 2021."
            document.getElementById("missedDeadline").text() shouldBe "Because you missed this deadline by more than 30 days, you have been charged a late payment penalty. This penalty is made up of two parts."
            document.getElementById("reasonList").getElementsByTag("li").get(0).text() shouldBe "3% of £99.99 (the tax that was outstanding 15 days after the payment deadline)"
            document.getElementById("reasonList").getElementsByTag("li").get(1).text() shouldBe "An additional 3% of £99.99 (the tax that was outstanding 30 days after the payment deadline)"
            //this message is incorrect. it should be empty
            document.getElementById("penaltyStatus") shouldBe null



          }
        }
      }

//        "render the first late payment calculation page" in {
//          stubAuthRequests(isAgent)
//          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(samplePenaltyDetailsModel))
//
//          val result = get(firstLPPPath, isAgent)
//          result.status shouldBe OK
//
//          val document = Jsoup.parse(result.body)
//
//          document.getServiceName.text() shouldBe "Manage your Self Assessment"
//          document.title() shouldBe "First late payment penalty calculation - Manage your Self Assessment - GOV.UK"
//          document.getH1Elements.text() shouldBe "First late payment penalty calculation"
//          document.getParagraphs.get(0).text() shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
//          document.getParagraphs.get(1).text() shouldBe "It is made up of 2 parts:"
//          document.getBulletPoints.get(0).text() shouldBe "2% of £20,000 (the unpaid Income Tax 15 days after the due date)"
//          document.getBulletPoints.get(1).text() shouldBe "2% of £20,000 (the unpaid Income Tax 30 days after the due date)"
//          document.getSummaryListQuestion.get(0).text() shouldBe "Penalty amount"
//          document.getSummaryListQuestion.get(1).text() shouldBe "Amount received"
//          document.getSummaryListQuestion.get(2).text() shouldBe "Left to pay"
//          document.getSummaryListAnswer.get(0).text() shouldBe "£800.00"
//          document.getSummaryListAnswer.get(1).text() shouldBe "£800.00"
//          document.getSummaryListAnswer.get(2).text() shouldBe "£0.00"
//          document.getLink("returnToIndex").text() shouldBe "Return to Self Assessment penalties and appeals"
//        }
//      }

      "a penalty does not exist for the penaltyId" should {
        "redirect to penalties home" in {
          stubAuthRequests(isAgent)
          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(emptyPenaltyDetailsModel))

          val result = get(firstLPPPath, isAgent)
          result.status shouldBe SEE_OTHER
          result.header("Location") shouldBe Some(routes.IndexController.homePage(isAgent).url)
        }
      }
    }

//    s"GET $secondLPPPath" when {
//      "a second late payment penalty exists for the penaltyId" should {
//        "render the second late payment calculation page" in {
//          stubAuthRequests(isAgent)
//          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(samplePenaltyDetailsLPP2Model))
//
//          val result = get(secondLPPPath, isAgent)
//          result.status shouldBe OK
//
//          val document = Jsoup.parse(result.body)
//
//          document.getServiceName.text() shouldBe "Manage your Self Assessment"
//          document.title() shouldBe "Second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
//          document.getH1Elements.text() shouldBe "Second late payment penalty calculation"
//          document.getParagraphs.get(0).text() shouldBe "This penalty applies if Income Tax has not been paid for 30 days."
//          document.getParagraphs.get(1).text() shouldBe "It is made up of 2 parts:"
//          document.getBulletPoints.get(0).text() shouldBe "2% of £20,000 (the unpaid Income Tax 15 days after the due date)"
//          document.getBulletPoints.get(1).text() shouldBe "2% of £20,000 (the unpaid Income Tax 30 days after the due date)"
//          document.getSummaryListQuestion.get(0).text() shouldBe "Penalty amount"
//          document.getSummaryListQuestion.get(1).text() shouldBe "Amount received"
//          document.getSummaryListQuestion.get(2).text() shouldBe "Left to pay"
//          document.getSummaryListAnswer.get(0).text() shouldBe "£800.00"
//          document.getSummaryListAnswer.get(1).text() shouldBe "£800.00"
//          document.getSummaryListAnswer.get(2).text() shouldBe "£0.00"
//          document.getLink("returnToIndex").text() shouldBe "Return to Self Assessment penalties and appeals"
//        }
//      }
//
//      "a penalty does not exist for the penaltyId" should {
//        "redirect to penalties home" in {
//          stubAuthRequests(isAgent)
//          stubGetPenalties(testAgentNino, optArn)(OK, Json.toJson(emptyPenaltyDetailsModel))
//
//          val result = get(firstLPPPath, isAgent)
//          result.status shouldBe SEE_OTHER
//          result.header("Location") shouldBe Some(routes.IndexController.homePage(isAgent).url)
//        }
//      }
//    }
  }
}
