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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.components

import fixtures.PenaltiesDetailsTestData
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.CalculationFooterLinks

class CalculationFooterLinksSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with PenaltiesDetailsTestData {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val calculationFooterLinks: CalculationFooterLinks = app.injector.instanceOf[CalculationFooterLinks]
  implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "CalculationFooterLinks" when {

    Seq(true, false).foreach { isAgent =>

      s"isAgent = $isAgent" when {

        "rendering with FirstLatePaymentPenaltyCalculationData" when {

          val calculationData = sampleFirstLPPCalcData()

          "hasFinancialCharge is true" should {

            "render the 'Check what you owe' link" in {
              implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))

              val html = calculationFooterLinks(calculationData, isAgent, hasFinancialCharge = true)
              val document = Jsoup.parse(html.toString)

              document.select("#returnToIndex").text() shouldBe msgs("calculation.return.link")
              document.select("#returnToIndex").attr("href") shouldBe
                uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.homePage(isAgent = isAgent).url

              document.select("#checkWhatYouOweLink").text() shouldBe msgs("calculation.checkWhatYouOwe.link")
              document.select("#checkWhatYouOweLink").attr("href") shouldBe appConfig.checkWhatYouOweUrl(isAgent)
            }
          }

          "hasFinancialCharge is false" should {

            "not render the 'Check what you owe' link" in {
              implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))

              val html = calculationFooterLinks(calculationData, isAgent, hasFinancialCharge = false)
              val document = Jsoup.parse(html.toString)

              document.select("#returnToIndex").text() shouldBe msgs("calculation.return.link")
              document.select("#returnToIndex").attr("href") shouldBe
                uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.homePage(isAgent = isAgent).url

              document.select("#checkWhatYouOweLink").isEmpty shouldBe true
            }
          }
        }

        "rendering with SecondLatePaymentPenaltyCalculationData" when {

          val calculationData = sampleSecondLPPCalcData()

          "hasFinancialCharge is true" should {

            "render the 'Check what you owe' link" in {
              implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))

              val html = calculationFooterLinks(calculationData, isAgent, hasFinancialCharge = true)
              val document = Jsoup.parse(html.toString)

              document.select("#returnToIndex").text() shouldBe msgs("calculation.return.link")
              document.select("#returnToIndex").attr("href") shouldBe
                uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.homePage(isAgent = isAgent).url

              document.select("#checkWhatYouOweLink").text() shouldBe msgs("calculation.checkWhatYouOwe.link")
              document.select("#checkWhatYouOweLink").attr("href") shouldBe appConfig.checkWhatYouOweUrl(isAgent)
            }
          }

          "hasFinancialCharge is false" should {

            "not render the 'Check what you owe' link" in {
              implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))

              val html = calculationFooterLinks(calculationData, isAgent, hasFinancialCharge = false)
              val document = Jsoup.parse(html.toString)

              document.select("#returnToIndex").text() shouldBe msgs("calculation.return.link")
              document.select("#returnToIndex").attr("href") shouldBe
                uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.routes.IndexController.homePage(isAgent = isAgent).url

              document.select("#checkWhatYouOweLink").isEmpty shouldBe true
            }
          }
        }
      }
    }
  }
}

