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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.indexPage.lpp

import org.jsoup.nodes.Document
import uk.gov.hmrc.incometaxpenaltiesfrontend.controllers.helpers.ControllerISpecHelper
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.UserDetailsData
import uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users.lpp.*

trait LPPControllerHelper extends ControllerISpecHelper {

  val lppUsers: Map[String, UserDetailsData] = Map(
    "AA100000A" -> AA100000A,
    "AA100000B" -> AA100000B,
    "AA100000C" -> AA100000C,
    "AA100000D" -> AA100000D,
    "AA100002C" -> AA100002C,
    "AA120000C" -> AA120000C,
    "AA123450A" -> AA123450A,
    "AA200000A" -> AA200000A,
    "AA200000B" -> AA200000B,
    "AA200000C" -> AA200000C,
    "AA200010A" -> AA200010A,
    "AA222220A" -> AA222220A,
    "AA233330A" -> AA233330A,
    "AA233440A" -> AA233440A,
    "AA244440A" -> AA244440A,
    "AC100000A" -> AC100000A,
    "AC100000B" -> AC100000B,
    "AC200000A" -> AC200000A,
    "AC200000B" -> AC200000B,
    "AL200001A" -> AL200001A,
    "AL300001A" -> AL300001A,
    "AL300002A" -> AL300002A,
    "AL300003A" -> AL300003A,
    "PE000002A" -> PE000002A,
    "AA200000C-overdue" -> AA200000COverdue,
    "AA233330A-overdue" -> AA233330AOverdue,
    "AA233440A-overdue" -> AA233440AOverdue,
    "AL200001A-overdue" -> AL200001AOverdue,
    "AA222220A-overdue" -> AA222220AOverdue,
    "AA200010A-overdue" -> AA200010AOverdue,
    "AA100000B-overdue" -> AA100000BOverdue
  )

  def validatePenaltyOverview(document: Document, expectedContent: String, isAgent: Boolean = false) = {
    if (expectedContent.equals("")) {
      document.getElementById("penaltiesOverview") shouldBe null
    } else {
      val overview = document.getElementById("penaltiesOverview")
      document.getH2Elements.get(0).text() shouldBe "Overview"
      overview.text() shouldBe expectedContent
      document.getH2Elements.get(1).text() shouldBe "Penalty and appeal details"
      document.getSubmitButton.text() shouldBe s"Check amounts${if (isAgent) "" else " and pay"}"
    }
  }

  def validateNoLSPPenalties(document: Document, isAgent: Boolean = false) = {
    val lspTabContent = getLSPTabContent(document)
    lspTabContent.getElementById("lspHeading").text() shouldBe "Late submission penalties"
    val expectedLSPContent = if(isAgent){
      "Your client has no active late submission penalties."
    } else{
      "You don’t have any active late submission penalties."
    }
    lspTabContent.getElementsByClass("govuk-body").first().text() shouldBe expectedLSPContent
  }

}
