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

package fixtures.messages

object IndexViewMessages {

  sealed trait Messages { this: i18n =>
    val noLSP = "You don’t have any active late submission penalties"
    val noLSPAgent = "Your client has no active late submission penalties."
    val noLPPIndividual = "You have no late payment penalties that are currently due"
    val noLPPAgent = "Your client has no late payment penalties that are currently due."
    val overviewH2 = "Overview"
    val overviewP1: Boolean => String = {
      case true => "Your client’s account has:"
      case false => "Your account has:"
    }
    val overviewP1NoBullets: Boolean => String => String = {
      isAgent =>
        message =>
          if (isAgent) s"Your client’s account has $message"
          else s"Your account has $message"
    }
    val overviewLSPPoints: Int => String = {
      case 1 => "1 late submission penalty point"
      case n => s"$n late submission penalty points"
    }

    def overviewLSPPointsNoBullets(num: Int, isAgent: Boolean = false): String = {
      num match {
        case 1 => if (isAgent) "Your client’s account has 1 late submission penalty point" else "Your account has 1 late submission penalty point"
        case n => if (isAgent) s"Your client’s account has $n late submission penalty points" else s"Your account has $n late submission penalty points"
      }
    }

    val overviewLSPFinancial: Int => String = {
      case 1 => "a late submission penalty"
      case _ => "late submission penalties"
    }

    val overviewLPP: Int => String = {
      case 1 => "a late payment penalty"
      case _ => s"late payment penalties"
    }

    val overviewLPPNoBullets: Int => String = {
      case 1 => "Your account has a late payment penalty"
      case _ => s"Your account has late payment penalties"
    }

    val overviewLSPPointsMax: String = "the maximum number of late submission penalty points"
    val overviewOverdueTaxCharge: String = "overdue Income Tax charges"
    val overviewInterest: String = "unpaid interest"
    val overviewCheckAndPay: String = "Check what you owe"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val noLPPIndividual = "Nid oes gennych unrhyw gosbau am dalu’n hwyr ar hyn o bryd."
    override val noLPPAgent = "Nid oes gan eich cleient unrhyw gosbau am dalu’n hwyr ar hyn o bryd."
    override val noLSP = "Cosbau am gyflwyno’n hwyr Nid oes unrhyw gosbau am gyflwyno’n hwyr ar waith gennych ar hyn o bryd"
    override val noLSPAgent = "Nid oes gan eich cleient unrhyw gosbau actif am gyflwyno’n hwyr ar hyn o bryd."
    override val overviewH2 = "Trosolwg"
    override val overviewP1: Boolean => String = {
      case true => "Mae gan gyfrif eich cleient y canlynol:"
      case false => "Mae gan eich cyfrif y canlynol:"
    }
    override val overviewP1NoBullets: Boolean => String => String = {
      isAgent =>
        message =>
          if (isAgent) s"Mae gan gyfrif eich cleient y canlynol $message"
          else s"Mae gan eich cyfrif y canlynol $message"
    }
    override val overviewLSPPoints: Int => String = {
      case 1 => "1 pwynt cosb am gyflwyno’n hwyr"
      case n => s"$n o bwyntiau cosb am gyflwyno’n hwyr"
    }

    override def overviewLSPPointsNoBullets(num: Int, isAgent: Boolean = false): String = {
      num match {
        case 1 => if (isAgent) "Mae gan gyfrif eich cleient 1 pwynt cosb am gyflwyno’n hwyr" else "Mae gan eich cyfrif 1 pwynt cosb am gyflwyno’n hwyr"
        case n => if (isAgent) s"Mae gan gyfrif eich cleient $n bwynt cosb am gyflwyno’n hwyr" else s"Mae gan eich cyfrif $n bwynt cosb am gyflwyno’n hwyr"
      }
    }

    override val overviewLSPFinancial: Int => String = {
      case 1 => "cosb am gyflwyno’n hwyr"
      case _ => "cosbau am gyflwyno’n hwyr"
    }

    override val overviewLPP: Int => String = {
      case 1 => "cosb am dalu’n hwyr"
      case _ => "cosbau am dalu’n hwyr"
    }

    override val overviewLPPNoBullets: Int => String = {
      case 1 => "Mae gan eich cyfrif gosb am dalu’n hwyr"
      case _ => "Mae gan eich cyfrif gosbau am dalu’n hwyr"
    }

    override val overviewLSPPointsMax: String = "uchafswm nifer y pwyntiau cosb am gyflwyno’n hwyr"
    override val overviewOverdueTaxCharge: String = "taliadau Treth Incwm gorddyledus"
    override val overviewInterest: String = "llog sydd heb ei dalu"
    override val overviewCheckAndPay: String = "Gwirio’r hyn sydd arnoch"
  }
}
