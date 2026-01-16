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
      "AA500000A" -> AA500000A,
      "AA211110A" -> AA211110A,
      "AA211120A" -> AA211120A,
      "AA211130A" -> AA211130A,
      "AB211110A" -> AB211110A,
      "AB211120A" -> AB211120A,
      "PE000000A" -> PE000000A,
      //LPS1
      "AA111110A" -> AA111110A
//      "AA111110B" -> AA111110B,
//      "AA111120A" -> AA111120A,
//      "AA111121A" -> AA111121A,
//      "AA111122A" -> AA111122A,
//      "AA111130A" -> AA111130A,
//      "AA111131A" -> AA111131A,
//      "AA111132A" -> AA111132A,
//      "AA121110A" -> AA121110A,
//      "AB111110A" -> AB111110A,
//      "AB111110B" -> AB111110B,
//      "AB111120A" -> AB111120A,
//      "AB111121A" -> AB111121A,
//      "AB111122A" -> AB111122A,
//      "AB111130A" -> AB111130A,
//      "AB111131A" -> AB111131A,
//      "AB111132A" -> AB111132A,
//      "AB121110A" -> AB121110A
    )
  }

  def validatePenaltyOverview(document: Document, expectedOverview: String, hasFinancialLSP: Boolean, isAgent: Boolean = false) = {
    val overview = document.getElementById("penaltiesOverview")
    overview.getElementById("overviewHeading").text() shouldBe "Overview"
    overview.text() shouldBe expectedOverview
    document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
    if (hasFinancialLSP) {
      document.getSubmitButton.text() shouldBe s"Check amounts${if(isAgent) "" else " and pay"}"
    }
  }

  def validateNoLPPPenalties(document: Document, isAgent: Boolean = false) = {
    val lppTabContent = getLPPTabContent(document)
    lppTabContent.getElementById("lppHeading").text() shouldBe "Late payment penalties"
    val expectedLSPContent = if(isAgent){
      "Your client has no late payment penalties that are currently due."
    } else{
      "You have no late payment penalties that are currently due."
    }
    lppTabContent.getElementsByClass("govuk-body").first().text() shouldBe expectedLSPContent
  }

  def expectedLSPTabBody(userDetailsData: UserDetailsData, isAgent: Boolean = false): String = {
    if (userDetailsData.numberOfLSPPenalties == 0) {
      if (isAgent) {
        "Your client has no active late submission penalties."
      } else {
        "You don’t have any active late submission penalties."
      }
    } else if(userDetailsData.hasFinanicalLSP) {
      if (userDetailsData.numberOfFinancialPenalties == 1) {
        if (isAgent) {
          "They will get an additional £200 penalty every time they send a late submission in the future, until their points are removed." +
            " They should send any missing submissions as soon as possible if they haven’t already."
        } else {
          "You will get an additional £200 penalty every time you send a late submission in the future, until your points are removed." +
            " You should send any missing submissions as soon as possible if you haven’t already."
        }
      } else {
        if (isAgent) {
          "They will get another £200 penalty every time they send a late submission in the future, until their points are removed." +
            " They should send any missing submissions as soon as possible if they haven’t already."
        } else {
          "You will get another £200 penalty every time you send a late submission in the future, until your points are removed." +
            " You should send any missing submissions as soon as possible if you haven’t already."
        }
      }
    } else if(userDetailsData.numberOfLSPPenalties == 1) {
      if (isAgent) {
        s"Your client has 1 penalty point for sending a late submission." +
          s" They should send this missing submission as soon as possible if they haven’t already."
      } else {
        s"You have 1 penalty point for sending a late submission." +
          s" You should send this missing submission as soon as possible if you haven’t already."
      }
    } else {
      val numPenPoints = userDetailsData.numberOfLSPPenalties.toString
      if(isAgent) {
        s"Your client has ${numPenPoints} penalty points for sending late submissions." +
          s" They should send any missing submissions as soon as possible if they haven’t already."
      } else {
        s"You have ${numPenPoints} penalty points for sending late submissions." +
          s" You should send any missing submissions as soon as possible if you haven’t already."
      }
    }
  }

}
