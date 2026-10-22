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

package uk.gov.hmrc.incometaxpenaltiesfrontend.views.partials

import fixtures.PenaltiesDetailsTestData
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.ViewBehaviours
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.partials.Lpp1CalculationTable

class Lpp1CalculationTableSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with PenaltiesDetailsTestData with ViewBehaviours {

  private lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private lazy val lpp1CalculationTable: Lpp1CalculationTable = app.injector.instanceOf[Lpp1CalculationTable]

  "Lpp1CalculationTable" should {
    "the penalty is partially paid" when {
      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))
      val calculationData = sampleFirstLPPCalcData(is15to30Days = false, isEstimate = false, isPartiallyPaid = true).copy(
        penaltyAmountPaid = Some(70),
        penaltyAmountOutstanding = Some(50)
      )
      implicit val document: Document = asDocument(lpp1CalculationTable(calculationData))

      "render the payment details heading" in {
        document.getElementById("paymentDetailsHeading").text() shouldBe "Your payment details"
      }

      "render two tables" in {
        document.select(".govuk-table").size() shouldBe 2
      }

      "render the main calculation table rows" in {
        val mainTable = document.select(".govuk-table").get(0)

        mainTable.select("thead th").get(0).text() shouldBe "Amount penalty is applied to"
        mainTable.select("thead th").get(1).text() shouldBe "Days after payment deadline"
        mainTable.select("thead th").get(2).text() shouldBe "Rate"
        mainTable.select("thead th").get(3).text() shouldBe "Amount"

        mainTable.select("tbody tr").get(0).select("th,td").get(0).text() should include("2,000.00")
        mainTable.select("tbody tr").get(0).select("th,td").get(1).text() should include("15")
        mainTable.select("tbody tr").get(0).select("th,td").get(2).text() should include("3.0%")
        mainTable.select("tbody tr").get(0).select("th,td").get(3).text() should include("60.00")

        mainTable.select("tbody tr").get(1).select("th,td").get(0).text() should include("2,000.00")
        mainTable.select("tbody tr").get(1).select("th,td").get(1).text() should include("30")
        mainTable.select("tbody tr").get(1).select("th,td").get(2).text() should include("3.0%")
        mainTable.select("tbody tr").get(1).select("th,td").get(3).text() should include("60.00")

        mainTable.select("tbody tr").get(2).select("th,td").get(0).text() should include("Total penalty")
        mainTable.select("tbody tr").get(2).select("th,td").get(3).text() should include("120.00")
      }

      "render the your payment details table rows" in {
        val paymentDetailsTable = document.select(".govuk-table").get(1)

        document.getElementById("paymentDetailsHeading").text() shouldBe "Your payment details"

        paymentDetailsTable.select("tbody tr").get(0).select("th,td").get(0).text() should include("Total penalty")
        paymentDetailsTable.select("tbody tr").get(0).select("th,td").get(3).text() should include("120.00")

        paymentDetailsTable.select("tbody tr").get(1).select("th,td").get(0).text() should include("Amount paid")
        paymentDetailsTable.select("tbody tr").get(1).select("th,td").get(3).text() should include("70.00")

        paymentDetailsTable.select("tbody tr").get(2).select("th,td").get(0).text() should include("Still to pay")
        paymentDetailsTable.select("tbody tr").get(2).select("th,td").get(3).text() should include("50.00")
      }
    }

    "the penalty is not partially paid" when {
      implicit val msgs: Messages = messagesApi.preferred(Seq(Lang("en")))
      val calculationData = sampleFirstLPPCalcData(isEstimate = false)
      implicit val document: Document = asDocument(lpp1CalculationTable(calculationData))

      "not render the payment details heading" in {
        document.getElementById("paymentDetailsHeading") shouldBe null
      }

      "render only one table" in {
        document.select(".govuk-table").size() shouldBe 1
      }

      "render the main table rows" in {
        val mainTable = document.select(".govuk-table").get(0)

        mainTable.select("thead th").get(0).text() shouldBe "Amount penalty is applied to"
        mainTable.select("thead th").get(1).text() shouldBe "Days after payment deadline"
        mainTable.select("thead th").get(2).text() shouldBe "Rate"
        mainTable.select("thead th").get(3).text() shouldBe "Amount"

        mainTable.select("tbody tr").get(0).select("th,td").get(0).text() should include("2,000.00")
        mainTable.select("tbody tr").get(0).select("th,td").get(1).text() should include("15")
        mainTable.select("tbody tr").get(0).select("th,td").get(2).text() should include("3.0%")
        mainTable.select("tbody tr").get(0).select("th,td").get(3).text() should include("60.00")

        mainTable.select("tbody tr").get(1).select("th,td").get(0).text() should include("Total penalty")
        mainTable.select("tbody tr").get(1).select("th,td").get(3).text() should include("60.00")
      }
    }
  }
}
