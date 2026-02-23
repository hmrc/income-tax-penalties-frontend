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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.featureswitch.core.config.{FeatureSwitching, UseStubForBackend}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.ExpiryReasonEnum.{Appeal, NaturalExpiration}
import uk.gov.hmrc.incometaxpenaltiesfrontend.models.penaltyDetails.lsp.LSPPenaltyStatusEnum
import uk.gov.hmrc.incometaxpenaltiesfrontend.stubs.PenaltiesStub
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
class IndexControllerISpec extends ControllerISpecHelper with FeatureSwitching
  with PenaltiesStub with PenaltiesDetailsTestData {

  override val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(UseStubForBackend)
  }

  "GET /view-penalty/self-assessment" when {
    "the call to penalties backend returns data" should {
      "have the correct page has correct elements" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          stubGetPenalties(defaultNino, None)(OK, convertPenaltyDetailsToSuccessJsonResponse(samplePenaltyDetailsModel))

          val result = get("/")

          val document = Jsoup.parse(result.body)

          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
          document.getH2Elements.get(0).text() shouldBe "Overview"
          document.getParagraphs.get(0).text() shouldBe "Your account has:"
          document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
          document.getH3Elements.get(0).text() shouldBe "Late submission penalties"
          document.getH3Elements.get(1).text() shouldBe "Late payment penalties"
          document.getSubmitButton.text() shouldBe "Check what you owe"
        }

        "the user is an authorised agent" in {
          stubAuthRequests(true)
          stubGetPenalties(defaultNino, Some("123456789"))(OK, convertPenaltyDetailsToSuccessJsonResponse(samplePenaltyDetailsModel))

          val result = get("/agent", isAgent = true)

          val document = Jsoup.parse(result.body)

          document.getServiceName.get(0).text() shouldBe "Manage your Self Assessment"
          document.title() shouldBe "Self Assessment penalties and appeals - Manage your Self Assessment - GOV.UK"
          document.getH1Elements.text() shouldBe "Self Assessment penalties and appeals"
          document.getH2Elements.get(0).text() shouldBe "Overview"
          document.getParagraphs.get(0).text() shouldBe "Your client’s account has:"
          document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
          document.getH3Elements.get(0).text() shouldBe "Late submission penalties"
          document.getH3Elements.get(1).text() shouldBe "Late payment penalties"
          document.getSubmitButton.text() shouldBe "Check what you owe"
        }
      }
    }

    "the call to the backend includes cancelled penalties" should {
      val createdDate1 = creationDate
      val createdDate2 = createdDate1.plusMonths(4)
      val createdDate3 = createdDate2.plusMonths(4)
      val lspDetails1 = sampleLateSubmissionPoint.copy(
        penaltyNumber = "1234567890",
        penaltyCreationDate = createdDate1,
        penaltyStatus = LSPPenaltyStatusEnum.Inactive,
        expiryReason = Some(NaturalExpiration)
      )
      val lspDetails2 = sampleLateSubmissionPoint.copy(
        penaltyNumber = "0987654321",
        penaltyCreationDate = createdDate2,
        penaltyStatus = LSPPenaltyStatusEnum.Active
      )
      val lspDetails3 = lspDetails1.copy(
        penaltyNumber = "1122334455",
        penaltyCreationDate = createdDate3,
        penaltyStatus = LSPPenaltyStatusEnum.Inactive,
        expiryReason = Some(Appeal),
        appealInformation = Some(Seq(AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Upheld),
          appealLevel = Some(AppealLevelEnum.FirstStageAppeal)
        )))
      )
      val lsp = lateSubmissionPenalty.copy(details = Seq(lspDetails3, lspDetails1, lspDetails2))
      val penaltyDetailsWithCancelledPenalties = samplePenaltyDetailsModel.copy(lateSubmissionPenalty = Some(lsp), latePaymentPenalty = None)
      "render the page with penalties in the correct order" when {
        "the user is an authorised individual" in {
          stubAuthRequests(false)
          stubGetPenalties(defaultNino, None)(OK, convertPenaltyDetailsToSuccessJsonResponse(penaltyDetailsWithCancelledPenalties))

          val result = get("/")

          val document = Jsoup.parse(result.body)

          val penaltyCards = document.getElementsByClass("govuk-summary-card__title").eachText()

          penaltyCards.size shouldBe 3
          penaltyCards.get(0) shouldBe "Penalty point"
          penaltyCards.get(1) shouldBe  "Penalty point 1: Late update"
          penaltyCards.get(2) shouldBe  "Penalty point"

        }
      }
    }

    "when call to penalties backend fails" should {
      "render an ISE" in {
        stubAuthRequests(false)
        stubGetPenalties(defaultNino, None)(INTERNAL_SERVER_ERROR, Json.obj())

        val result = get("/")

        result.status shouldBe INTERNAL_SERVER_ERROR
        result.body should include("Sorry, there is a problem with the service")
      }
    }
  }
}
