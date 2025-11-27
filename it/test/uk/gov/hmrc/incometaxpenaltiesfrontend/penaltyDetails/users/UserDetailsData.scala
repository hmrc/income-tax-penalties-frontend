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

package uk.gov.hmrc.incometaxpenaltiesfrontend.penaltyDetails.users

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsValue, Json}

import java.io.{File, FileWriter}
import scala.io.Source
import scala.util.{Failure, Success, Try}

trait UserDetailsData extends Matchers {
  
  val nino: String
  val numberOfLSPPenalties: Int = 0
  val expectedNumberOfLSPPenaltyCards: Int
  val expectedNumberOfLPPPenaltyCards: Int
  val hasFinanicalLSP: Boolean = false

  lazy val apiResponse: JsValue = getApiResponseJson(nino)

  val expectedPenaltyCardsContent: Map[Int, Element => Unit]
  
  val expectedOverviewText: Boolean => String

  def getFilePath(nino: String): String = {
    s"it/test/uk/gov/hmrc/incometaxpenaltiesfrontend/penaltyDetails/apiResponses/$nino.json"
  }

  def getApiResponseJson(nino: String): JsValue = {
    Try {
      val filePath = getFilePath(nino)
      val source = Source.fromFile(filePath)
      val payloadString = source.getLines().mkString
      val jsonPayload = Json.parse(payloadString)
      (jsonPayload \ "payload").get
    } match {
      case Success(json) => json
      case Failure(exception) => throw new RuntimeException(s"Could not read API response JSON for NINO $nino: ${exception.getMessage}")
    }
  }

  def validatePenaltyCardsContent(cards: Elements): Unit = {
    expectedPenaltyCardsContent.foreach{ case (index, validateFunction) =>
      val card = cards.get(index)
      validateFunction(card)
    }
  }

  def validatePenaltyCardTitle(card: Element, expectedTitle: String): Unit = {
    val title = card.getElementsByClass("govuk-summary-card__title").first().text()
    title shouldBe expectedTitle
  }

  def validateCardTag(card: Element, expectedTag: String): Unit = {
    val expectedTagClass = expectedTag match {
      case "Paid" => "govuk-tag govuk-tag--green"
      case x if x.toLowerCase.contains("due") => "govuk-tag govuk-tag--red"
      case _ => "govuk-tag"
    }
    val tag = card.getElementsByClass(expectedTagClass).first()
    tag.text() shouldBe expectedTag
  }

  def getCardsRows(card: Element): Elements = {
    card.getElementsByClass("govuk-summary-list__row govuk-summary-list__row")
  }

  def validateSummary(summaryRow: Element, expectedKey: String, expectedContent: String): Unit = {
    val key = summaryRow.getElementsByClass("govuk-summary-list__key govuk-summary-list__key").first().text()
    val content = summaryRow.getElementsByClass("govuk-summary-list__value govuk-summary-list__value").first().text()
    key shouldBe expectedKey
    content shouldBe expectedContent
  }

  def validateViewCalculationLink(card: Element, penaltyIndex: Int, isSecondLPP: Boolean = false): Unit = {
    val linkId = "lpp-view-calculation-link-" + penaltyIndex.toString
    val link = card.getElementById(linkId)
    val expectedHref = if (isSecondLPP) {
      s"second-lpp-calculation?penaltyId="
    } else {
      s"first-lpp-calculation?penaltyId="
    }
    link.text() shouldBe "View calculation"
    link.attr("href") should include(expectedHref)
  }
  
  def validateAppealLink(link: Element, is2ndStage: Boolean = false): Unit = {
    val expectedHref = "appeal-penalty"
    val expectedLinkContent = if(is2ndStage) {
      "Ask for review"
    } else {
      "Check if you can appeal this penalty"
    }
    link.text() shouldBe expectedLinkContent
    link.attr("href") should include(expectedHref)
    if(is2ndStage) {
      link.attr("href") should include("is2ndStageAppeal=true")
    }
  }

}
