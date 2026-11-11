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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views

import fixtures.PenaltiesDetailsTestData
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.utils.TimeMachine
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.Lpp1Calculation

class Lpp1CalculationSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with PenaltiesDetailsTestData {

  lazy val lpp1Calculation: Lpp1Calculation = app.injector.instanceOf[Lpp1Calculation]
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit lazy val timeMachine: TimeMachine = app.injector.instanceOf[TimeMachine]
  implicit lazy val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  "Lpp1Calculation" should {
    "render only one Check what you owe link for an overdue unpaid revenue amendment" in {
      val isAgent = false
      val calculationData = sampleFirstLPPCalcData(isPenaltyPaid = false, isEstimate = false, isOverdue = true)
        .copy(chargeClassificationValue = Some("RA"))

      val html = lpp1Calculation(
        calculationData = calculationData,
        isAgent = isAgent,
        timeMachine = timeMachine,
        inBreathingSpace = false,
        breathingSpaceData = None
      )
      val document = Jsoup.parse(html.toString)
      val checkWhatYouOweUrl = appConfig.checkWhatYouOweUrl(isAgent)
      val linksToCheckWhatYouOwe = document.select(s"a[href='$checkWhatYouOweUrl']")

      linksToCheckWhatYouOwe.size() shouldBe 1
      linksToCheckWhatYouOwe.first().text() shouldBe messages("calculation.penaltyDueDate.overDue.ra.p.link")
      document.select("#checkWhatYouOweLink").isEmpty shouldBe true
    }
  }
}
