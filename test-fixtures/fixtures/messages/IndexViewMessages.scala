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
    val noLPPIndividual = "You do not have any late payment penalties."
    val taxPaidButPenaltyNotPaid = "You can pay your penalties now."
    val overviewH2 = "Overview"
    val overviewP1: String = "Your account has:"

    val overviewLSPPoints: Int => String = {
      case 1 => "1 late submission penalty point"
      case n => s"$n late submission penalty points"
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
      case 1 => "a late payment penalty"
      case _ => "late payment penalties"
    }

    val overviewLSPPointsMax: String = "the maximum number of late submission penalty points"
    val overviewOverdueTaxCharge: String = "overdue Income Tax charges"
    val overviewInterest: String = "unpaid interest"
    val overviewCheckAndPay: String = "Check what you owe"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val noLPPIndividual = "Nid oes gennych unrhyw gosbau am dalu’n hwyr."
    override val noLSP = "Cosbau am gyflwyno’n hwyr Nid oes unrhyw gosbau am gyflwyno’n hwyr ar waith gennych ar hyn o bryd"
    override val taxPaidButPenaltyNotPaid = "Gallwch dalu’r cosbau nawr."
    override val overviewH2 = "Trosolwg"
    override val overviewP1: String = "Mae gan eich cyfrif y canlynol:"

    override val overviewLSPPoints: Int => String = {
      case 1 => "1 pwynt cosb am gyflwyno’n hwyr"
      case n => s"$n o bwyntiau cosb am gyflwyno’n hwyr"
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
      case 1 => "cosb am dalu’n hwyr"
      case _ => "cosbau am dalu’n hwyr"
    }

    override val overviewLSPPointsMax: String = "uchafswm nifer y pwyntiau cosb am gyflwyno’n hwyr"
    override val overviewOverdueTaxCharge: String = "taliadau Treth Incwm gorddyledus"
    override val overviewInterest: String = "llog sydd heb ei dalu"
    override val overviewCheckAndPay: String = "Gwirio’r hyn sydd arnoch"
  }
}
