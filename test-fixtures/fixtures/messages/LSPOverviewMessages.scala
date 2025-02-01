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

object LSPOverviewMessages {

  sealed trait Messages { _: i18n =>
    val pointsTotal: Int => String = n => s"Penalty points total: $n"

    //Messages when points are accruing < threshold
    val pointsAccruingP1: Boolean => Int => String = {
      case true => {
        //isAgent messages
        case 1 => "Your client has 1 penalty point for sending a late submission. They should send this missing submission as soon as possible if they haven’t already."
        case n => s"Your client has $n penalty points for sending late submissions. They should send any missing submissions as soon as possible if they haven’t already."
      }
      case false => {
        //individual messages
        case 1 => "You have 1 penalty point for sending a late submission. You should send this missing submission as soon as possible if you haven’t already."
        case n => s"You have $n penalty points for sending late submissions. You should send any missing submissions as soon as possible if you haven’t already."
      }
    }
    val pointsAccruingP2: Boolean => String = {
      case true => //Agent
        "They’ll get another point if they send another submission after a deadline has passed."
      case false => //Individual
        "You’ll get another point if you send another submission after a deadline has passed."
    }
    val pointsAccruingP3: Boolean => Int => String = {
      case true => //Agent
        n => s"Points usually expire after 24 months, but it can be longer if they keep sending late submissions. If they reach $n points, they’ll have to pay a £200 penalty."
      case false => //Individual
        n => s"Points usually expire after 24 months, but it can be longer if you keep sending late submissions. If you reach $n points, you’ll have to pay a £200 penalty."
    }
    val pointsAccruingWarning: Boolean => String = {
      case true => //Agent
        "! Warning Your client will get a £200 penalty if they send another late submission."
      case false => //Individual
        "! Warning You’ll get a £200 penalty if you send another late submission."
    }

    //Messages when points == threshold (1 financial penalty triggered)
    val penaltyP1: Boolean => String = {
      case true => //Agent
        "They will get an additional £200 penalty every time they send a late submission in the future, until their points are removed. They should send any missing submissions as soon as possible if they haven’t already."
      case false => //Individual
        "You will get an additional £200 penalty every time you send a late submission in the future, until your points are removed. You should send any missing submissions as soon as possible if you haven’t already."
    }
    val penaltyWarning: Boolean => String = {
      case true => //Agent
        "! Warning Your client has been given a £200 penalty for reaching the penalty threshold."
      case false => //Individual
        "! Warning You have been given a £200 penalty for reaching the penalty threshold."
    }

    //Messages when points == threshold (more than one financial penalty triggered)
    val additionalPenaltyP1: Boolean => String = {
      case true => //Agent
        "They will get another £200 penalty every time they send a late submission in the future, until their points are removed. They should send any missing submissions as soon as possible if they haven’t already."
      case false => //Individual
        "You will get another £200 penalty every time you send a late submission in the future, until your points are removed. You should send any missing submissions as soon as possible if you haven’t already."
    }
    val additionalPenaltyWarning: Boolean => String = {
      case true => //Agent
        "! Warning Your client has been given an additional £200 penalty."
      case false => //Individual
        "! Warning You have been given an additional £200 penalty."
    }

    val pointsGuidanceLink = "Read the guidance about late submission penalties"
    val actionsLink: Boolean => String = {
      case true =>
        "Actions your client must take to get their points removed"
      case false =>
        "Actions to take to get your points removed"
    }
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val pointsTotal: Int => String = n => s"Penalty points total: (Welsh) $n"

    //Messages when points are accruing < threshold
    override val pointsAccruingP1: Boolean => Int => String = {
      case true => { //Agent
        case 1 => "Your client has 1 penalty point for sending a late submission. They should send this missing submission as soon as possible if they haven’t already. (Welsh)"
        case n => s"Your client has $n penalty points for sending late submissions. They should send any missing submissions as soon as possible if they haven’t already. (Welsh)"
      }
      case false => { //Individual
        case 1 => "You have 1 penalty point for sending a late submission. You should send this missing submission as soon as possible if you haven’t already. (Welsh)"
        case n => s"You have $n penalty points for sending late submissions. You should send any missing submissions as soon as possible if you haven’t already. (Welsh)"
      }
    }
    override val pointsAccruingP2: Boolean => String = {
      case true => "They’ll get another point if they send another submission after a deadline has passed. (Welsh)"
      case false => "You’ll get another point if you send another submission after a deadline has passed. (Welsh)"
    }
    override val pointsAccruingP3: Boolean => Int => String = {
      case true => //Agent
        n => s"Points usually expire after 24 months, but it can be longer if they keep sending late submissions. If they reach $n points, they’ll have to pay a £200 penalty. (Welsh)"
      case false => //Individual
        n => s"Points usually expire after 24 months, but it can be longer if you keep sending late submissions. If you reach $n points, you’ll have to pay a £200 penalty. (Welsh)"
    }
    override val pointsAccruingWarning: Boolean => String = {
      case true => //Agent
        "! Warning Your client will get a £200 penalty if they send another late submission. (Welsh)"
      case false => //Individual
        "! Warning You’ll get a £200 penalty if you send another late submission. (Welsh)"
    }

    //Messages when points == threshold (1 penalty triggered)
    override val penaltyP1: Boolean => String = {
      case true => //Agent
        "They will get an additional £200 penalty every time they send a late submission in the future, until their points are removed. They should send any missing submissions as soon as possible if they haven’t already. (Welsh)"
      case false => //Individual
        "You will get an additional £200 penalty every time you send a late submission in the future, until your points are removed. You should send any missing submissions as soon as possible if you haven’t already. (Welsh)"
    }
    override val penaltyWarning: Boolean => String = {
      case true => //Agent
        "! Warning Your client has been given a £200 penalty for reaching the penalty threshold. (Welsh)"
      case false => //Individual
        "! Warning You have been given a £200 penalty for reaching the penalty threshold. (Welsh)"
    }

    //Messages when points == threshold (more than one financial penalty triggered)
    override val additionalPenaltyP1: Boolean => String = {
      case true => //Agent
        "They will get another £200 penalty every time they send a late submission in the future, until their points are removed. They should send any missing submissions as soon as possible if they haven’t already. (Welsh)"
      case false => //Individual
        "You will get another £200 penalty every time you send a late submission in the future, until your points are removed. You should send any missing submissions as soon as possible if you haven’t already. (Welsh)"
    }
    override val additionalPenaltyWarning: Boolean => String = {
      case true => //Agent
        "! Warning Your client has been given an additional £200 penalty. (Welsh)"
      case false => //Individual
        "! Warning You have been given an additional £200 penalty. (Welsh)"
    }

    override val pointsGuidanceLink = "Read the guidance about late submission penalties (Welsh)"
    override val actionsLink: Boolean => String = {
      case true =>
        "Actions your client must take to get their points removed (Welsh)"
      case false =>
        "Actions to take to get your points removed (Welsh)"
    }
  }
}
