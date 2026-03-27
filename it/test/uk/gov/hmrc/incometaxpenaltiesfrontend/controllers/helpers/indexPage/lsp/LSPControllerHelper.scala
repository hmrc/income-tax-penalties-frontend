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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.indexPage.lsp

import org.jsoup.nodes.Document
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.UserDetailsData
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lsp.*

trait LSPControllerHelper extends ControllerISpecHelper {
  
  val lspUsers: Map[String, UserDetailsData] = { 
    Map(
      //Both
      "PE000002A" -> PE000002A,
      //LSP0
      "AA000000A" -> AA000000A,
      "AA000000B" -> AA000000B,
      "AA000000C" -> AA000000C,
      "AA000040A" -> AA000040A,
      "AA000041A" -> AA000041A,
      "AA000042A" -> AA000042A,
      "AA000050A" -> AA000050A,
      "AB000000B" -> AB000000B,
      "AB000000C" -> AB000000C,
      "AB000040A" -> AB000040A,
      "AB000041A" -> AB000041A,
      "AB000042A" -> AB000042A,
      "AB000050A" -> AB000050A,
      //LSP1
      "AA111110A" -> AA111110A,
      "AA111110B" -> AA111110B,
      "AA111120A" -> AA111120A,
      "AA111121A" -> AA111121A,
      "AA111122A" -> AA111122A,
      "AA111130A" -> AA111130A,
      "AA111131A" -> AA111131A,
      "AA111132A" -> AA111132A,
      "AA121110A" -> AA121110A,
      "AB111110A" -> AB111110A,
      "AB111110B" -> AB111110B,
      "AB111120A" -> AB111120A,
      "AB111121A" -> AB111121A,
      "AB111122A" -> AB111122A,
      "AB111130A" -> AB111130A,
      "AB111131A" -> AB111131A,
      "AB111132A" -> AB111132A,
      "AB121110A" -> AB121110A,
      //LSP2
      "AA211110A" -> AA211110A,
      "AA211120A" -> AA211120A,
      "AA211130A" -> AA211130A,
      "AB211110A" -> AB211110A,
      "AB211120A" -> AB211120A,
      "PE000000A" -> PE000000A,
      "AB211110A-overdue" -> AB211110AOverdue,
      //LSP3
      "AA300000A" -> AA300000A,
      "AA311110A" -> AA311110A,
      "AB311110A" -> AB311110A,
      "AB311120A" -> AB311120A,
      "AB311130A" -> AB311130A,
      "AB311140A" -> AB311140A,
      //LSP4
      "AA400000A" -> AA400000A,
      "AA411110A" -> AA411110A,
      "AB400010A" -> AB400010A,
      "AB400020A" -> AB400020A,
      "AB411110A" -> AB411110A,
      "AB411145A" -> AB411145A,
      "PE000001A" -> PE000001A,
      "PE000003A" -> PE000003A,
      "AA400000A-overdue" -> AA400000AOverdue,
      "AA411110A-overdue" -> AA411110AOverdue,
      //LSP5
      "AA500000A" -> AA500000A,
      "AA500000B" -> AA500000B,
      "AA511110A" -> AA511110A,
      "AB500010A" -> AB500010A,
      "AB511110A" -> AB511110A,
      "AB511120A" -> AB511120A,
      "AB511130A" -> AB511130A,
      "AB511140A" -> AB511140A,
      "AB522240A" -> AB522240A,
      "AB611150A" -> AB611150A,
      "AA511110A-overdue" -> AA511110AOverdue,
      "AB511140A-overdue" -> AB511140AOverdue,
      "AB611150A-overdue" -> AB611150AOverdue
    )
  }

  def validatePenaltyOverview(document: Document, expectedOverview: String, hasUnpaidPenalty: Boolean): Unit = {
    if (expectedOverview.equals("")) {
      document.getElementById("penaltiesOverview") shouldBe null
    } else {
      val overview = document.getElementById("penaltiesOverview")
      overview.getElementById("overviewHeading").text() shouldBe "Overview"
      overview.text() shouldBe expectedOverview
      document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
      if (hasUnpaidPenalty) {
        document.getSubmitButton.text() shouldBe "Check what you owe"
      }
    }
  }

  def validateNoLPPPenalties(document: Document): Unit = {
    val lppTabContent = getLPPTabContent(document)
    lppTabContent.getElementById("lppHeading").text() shouldBe "Late payment penalties"
    val expectedLSPContent = "You have no late payment penalties that are currently due."
    lppTabContent.getElementsByClass("govuk-body").first().text() shouldBe expectedLSPContent
  }

  def expectedLSPTabBody(userDetailsData: UserDetailsData): String = {
    if (userDetailsData.numberOfLSPPenalties == 0) {
      "You don’t have any active late submission penalties."
    } else if (userDetailsData.hasFinancialLSP) {
      if ((userDetailsData.numberOfUnpaidFinancialPenalties + userDetailsData.numberOfPaidFinancialPenalties) == 1) {
        "You will get an additional £200 penalty every time you send a late submission in the future, until your points are removed." +
          " You should send any missing submissions as soon as possible if you haven’t already."
      } else {
        "You will get another £200 penalty every time you send a late submission in the future, until your points are removed." +
          " You should send any missing submissions as soon as possible if you haven’t already."
      }
    } else if (userDetailsData.numberOfLSPPenalties == 1) {
      s"You have 1 penalty point for sending a late submission." +
        s" You should send this missing submission as soon as possible if you haven’t already."

    } else {
      val numPenPoints = userDetailsData.numberOfLSPPenalties.toString
      s"You have $numPenPoints penalty points for sending late submissions." +
        s" You should send any missing submissions as soon as possible if you haven’t already."

    }
  }
}
