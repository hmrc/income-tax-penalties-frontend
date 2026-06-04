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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import fixtures.PenaltiesDetailsTestData
import org.jsoup.Jsoup
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lpp.LatePaymentPenalty

import java.time.LocalDate

class SupplementaryCalculationControllerISpec extends ControllerISpecHelper
  with PenaltiesStub with PenaltiesDetailsTestData with FeatureSwitching {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  def addQueryParam(pathNoQuery: String, penaltyId: String = principleChargeRef): String = {
    pathNoQuery + "?penaltyId=" + penaltyId
  }

  def getDateString(date: LocalDate): String = s"${date.getDayOfMonth} ${messagesAPI(s"month.${date.getMonthValue}")} ${date.getYear}"

  List(false, true).foreach { isAgent =>
    val pathStart = if (isAgent) "/agent-" else "/"
    val LPP1SupplementaryPath = addQueryParam(pathStart + "additional-first-lpp-calculation")
    val LPP2SupplementaryPath = addQueryParam(pathStart + "additional-second-lpp-calculation")
    val optArn = if (isAgent) Some("123456789") else None


    s"GET $LPP1SupplementaryPath" should {
      setFeatureDate(None)
      "render the supplementary calculation page for LPP1" when {
        "first late payment penalty supplementary charge exists for the penaltyId" in {
          stubAuthRequests(isAgent)
          val supplementary1LPPCalcData = sampleFirstLPPCalcData(isEstimate = false)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSupplementaryCalculationPagePage(supplementary1LPPCalcData)))
          val result = get(LPP1SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional first late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional first late payment penalty calculation"
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £60.00"
          document.getElementById("payPenaltyByDate").text() shouldBe s"Pay penalty by ${getDateString(supplementary1LPPCalcData.payPenaltyBy)}"
          document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementById("penaltyOverdue") shouldBe null
          document.getElementById("penaltyDue").text() shouldBe s"To avoid interest charges, you should pay this penalty by ${getDateString(supplementary1LPPCalcData.payPenaltyBy)}."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
          document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
          document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
        }
        "first late payment penalty supplementary charge exists for the penaltyId and is overdue" in {
          stubAuthRequests(isAgent)
          val supplementary1LPPCalcData = sampleFirstLPPCalcData(isOverdue = true, isEstimate = false)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSupplementaryCalculationPagePage(supplementary1LPPCalcData)))
          val result = get(LPP1SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional first late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional first late payment penalty calculation"
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £60.00"
          document.getElementById("payPenaltyByDate").text() shouldBe s"Pay penalty by ${getDateString(supplementary1LPPCalcData.payPenaltyBy)}"
          document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementById("penaltyDue") shouldBe null
          document.getElementById("penaltyOverdue").text() shouldBe "This penalty is overdue. We are charging interest."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
          document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
          document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
        }
        "first late payment penalty supplementary charge exists for the penaltyId and is paid" in {
          stubAuthRequests(isAgent)
          val supplementary1LPPCalcData = sampleFirstLPPCalcData(isPenaltyPaid = true, isEstimate = false)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSupplementaryCalculationPagePage(supplementary1LPPCalcData)))
          val result = get(LPP1SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional first late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional first late payment penalty calculation"
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £60.00"
          document.getElementById("penaltyPaid").text() shouldBe "Penalty paid"
          document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementById("penaltyOverdue") shouldBe null
          document.getElementById("penaltyDue") shouldBe null
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("PenaltyAmountDetailsP1").text() shouldBe "A first late payment penalty is made up of two parts."
          document.getElementById("PenaltyAmountDetailsP2").text() shouldBe "We charge:"
          document.getElementById("PenaltyAmountDetailsPoint1").text() shouldBe "3% of the unpaid Income Tax after 15 days"
          document.getElementById("PenaltyAmountDetailsPoint2").text() shouldBe "another 3% of the unpaid Income Tax after 30 days"
        }
        "render LPP1 supplementary page when a non-supplement LPP1 appears before the supplement LPP1 in the penalty response" in {
          stubAuthRequests(isAgent)
          val supplementary1LPPCalcData = sampleFirstLPPCalcData()
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsWithNonSupplementLPP1BeforeSupplementLPP1(supplementary1LPPCalcData)))
          val result = get(LPP1SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.title() shouldBe "Additional first late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional first late payment penalty calculation"
        }
      }
    }

    s"GET $LPP2SupplementaryPath" should {
      setFeatureDate(None)
      "render the supplementary calculation page for LPP2" when {
        "second late payment penalty supplementary charge exists for the penaltyId" in {
          stubAuthRequests(isAgent)
          val supplementary2LPPCalcData = sampleSecondLPPCalcData(isEstimate = false)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPageWithSupplement(supplementary2LPPCalcData)))
          val result = get(LPP2SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional second late payment penalty calculation"
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
          document.getElementById("payPenaltyByDate").text() shouldBe s"Pay penalty by ${getDateString(supplementary2LPPCalcData.payPenaltyBy)}"
          document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("yourPenaltyDetails").text() shouldBe "Your penalty details"
          document.getElementById("penaltyDue").text() shouldBe s"To avoid interest charges, you should pay this penalty by ${getDateString(supplementary2LPPCalcData.payPenaltyBy)}."
          document.getElementById("penaltyOverdue") shouldBe null
          document.getElementsByClass("govuk-summary-list__key").get(1).text() shouldBe "Annual rate"
          document.getElementsByClass("govuk-summary-list__value").get(1).text() shouldBe "10%"
          document.getElementsByClass("govuk-summary-list__key").get(2).text() shouldBe "Penalty amount"
          document.getElementsByClass("govuk-summary-list__value").get(2).text() shouldBe "£1,001.45"
        }
        "second late payment penalty supplementary charge exists for the penaltyId and is overdue" in {
          stubAuthRequests(isAgent)
          val supplementary2LPPCalcData = sampleSecondLPPCalcData(isOverdue = true, isEstimate = false)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPageWithSupplement(supplementary2LPPCalcData)))
          val result = get(LPP2SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional second late payment penalty calculation"
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
          document.getElementById("payPenaltyByDate").text() shouldBe s"Pay penalty by ${getDateString(supplementary2LPPCalcData.payPenaltyBy)}"
          document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("yourPenaltyDetails").text() shouldBe "Your penalty details"
          document.getElementById("penaltyOverdue").text() shouldBe "This penalty is overdue. We are charging interest."
          document.getElementById("penaltyDue") shouldBe null
          document.getElementsByClass("govuk-summary-list__key").get(1).text() shouldBe "Annual rate"
          document.getElementsByClass("govuk-summary-list__value").get(1).text() shouldBe "10%"
          document.getElementsByClass("govuk-summary-list__key").get(2).text() shouldBe "Penalty amount"
          document.getElementsByClass("govuk-summary-list__value").get(2).text() shouldBe "£1,001.45"
        }
        "second late payment penalty supplementary charge exists for the penaltyId and is paid" in {
          stubAuthRequests(isAgent)
          val supplementary2LPPCalcData = sampleSecondLPPCalcData(isPenaltyPaid = true, isEstimate = false)
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsForSecondCalculationPageWithSupplement(supplementary2LPPCalcData)))
          val result = get(LPP2SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Additional second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional second late payment penalty calculation"
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £1,001.45"
          document.getElementById("penaltyPaid").text() shouldBe "Penalty paid"
          document.getElementById("chargeReference").text() shouldBe "Charge reference: PEN1234567"
          document.getElementById("supplementaryReason").text() shouldBe "We issued this additional penalty because the unpaid tax amount used to calculate the earlier penalty was too low."
          document.getElementById("supplementaryAlert").text() shouldBe "You still need to pay the earlier penalty if you have not paid it."
          document.getElementsByClass("govuk-details__summary-text").text() shouldBe "How we work out the penalty amount"
          document.getElementById("yourPenaltyDetails").text() shouldBe "Your penalty details"
          document.getElementById("penaltyOverdue") shouldBe null
          document.getElementById("penaltyDue") shouldBe null
          document.getElementsByClass("govuk-summary-list__key").get(1).text() shouldBe "Annual rate"
          document.getElementsByClass("govuk-summary-list__value").get(1).text() shouldBe "10%"
          document.getElementsByClass("govuk-summary-list__key").get(2).text() shouldBe "Penalty amount"
          document.getElementsByClass("govuk-summary-list__value").get(2).text() shouldBe "£1,001.45"
        }
        "render LPP2 supplementary page when LPP1 appears before LPP2 in the penalty response" in {
          stubAuthRequests(isAgent)
          val supplementary2LPPCalcData = sampleSecondLPPCalcData()
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsWithLPP1BeforeLPP2Supplement(supplementary2LPPCalcData)))
          val result = get(LPP2SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.title() shouldBe "Additional second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional second late payment penalty calculation"
        }

        "render LPP2 supplementary page when a non-supplement LPP2 appears before the supplement LPP2 in the penalty response" in {
          stubAuthRequests(isAgent)
          val supplementary2LPPCalcData = sampleSecondLPPCalcData()
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(getPenaltyDetailsWithNonSupplementLPP2BeforeSupplementLPP2(supplementary2LPPCalcData)))
          val result = get(LPP2SupplementaryPath, isAgent)
          result.status shouldBe OK

          val document = Jsoup.parse(result.body)
          document.title() shouldBe "Additional second late payment penalty calculation - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Additional second late payment penalty calculation"
        }

        "render the selected LPP2 supplementary page when multiple supplements share the same principal charge reference" in {
          stubAuthRequests(isAgent)
          val selectedPenaltyChargeReference = "PEN1234568"
          val supplementary2LPPCalcData = sampleSecondLPPCalcData(isIncomeTaxPaid = true, isEstimate = false)
          val penaltyDetails = getPenaltyDetailsForSecondCalculationPageWithSupplement(supplementary2LPPCalcData)
          val responseWithMultipleAdditionalLpp2s = penaltyDetails.copy(
            penaltyDetails = penaltyDetails.penaltyDetails.map { details =>
              val baseAdditionalLpp2 = details.latePaymentPenalty.get.details.head
              val otherAdditionalLpp2 = baseAdditionalLpp2.copy(
                penaltyChargeReference = Some("PEN1234567"),
                penaltyAmountPosted = 200,
                penaltyAmountOutstanding = Some(200)
              )
              val selectedAdditionalLpp2 = baseAdditionalLpp2.copy(
                penaltyChargeReference = Some(selectedPenaltyChargeReference),
                penaltyAmountPosted = 300,
                penaltyAmountOutstanding = Some(300)
              )
              details.copy(latePaymentPenalty = Some(LatePaymentPenalty(Some(Seq(otherAdditionalLpp2, selectedAdditionalLpp2)))))
            }
          )
          stubGetPenalties(defaultNino, optArn)(OK, Json.toJson(responseWithMultipleAdditionalLpp2s))

          val result = get(addQueryParam(pathStart + "additional-second-lpp-calculation", selectedPenaltyChargeReference), isAgent)

          result.status shouldBe OK
          val document = Jsoup.parse(result.body)
          document.getElementById("penaltyAmount").text() shouldBe "Penalty amount: £300.00"
          document.getElementById("chargeReference").text() shouldBe s"Charge reference: $selectedPenaltyChargeReference"
        }
      }
    }
  }
}
