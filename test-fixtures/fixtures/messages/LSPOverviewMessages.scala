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
    val addedPointsGuidanceLink = "Read the guidance about adjustment points"

    val actionsLink: Boolean => String = {
      case true =>
        "Actions your client must take to get their points removed by April 2028"
      case false =>
        "Actions you must take to get your points removed by April 2028"
    }
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val pointsTotal: Int => String = n => s"Cyfanswm pwyntiau cosb: $n"

    //Messages when points are accruing < threshold
    override val pointsAccruingP1: Boolean => Int => String = {
      case true => { //Agent
        case 1 => "Mae gan eich cleient 1 pwynt cosb am gyflwyno’n hwyr. Dylai’ch cleient anfon y cyflwyniad sydd ar goll cyn gynted â phosibl os nad yw eisoes wedi gwneud hynny."
        case n => s"Mae gan eich cleient $n o bwyntiau cosb am gyflwyno’n hwyr. Dylai’ch cleient anfon unrhyw gyflwyniadau sy’n hwyr cyn gynted â phosibl os nad yw eisoes wedi gwneud hynny."
      }
      case false => { //Individual
        case 1 => "Mae gennych 1 pwynt cosb am gyflwyno’n hwyr. Dylech anfon y cyflwyniad sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
        case n => s"Mae gennych $n o bwyntiau cosb am gyflwyno’n hwyr. Dylech anfon unrhyw gyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
      }
    }
    override val pointsAccruingP2: Boolean => String = {
      case true => "Bydd yn cael pwynt arall os bydd yn anfon cyflwyniad arall ar ôl i’r dyddiad cau fynd heibio."
      case false => "Byddwch yn cael pwynt arall os byddwch yn anfon cyflwyniad arall ar ôl i’r dyddiad cau fynd heibio."
    }
    override val pointsAccruingP3: Boolean => Int => String = {
      case true => //Agent
        n => s"Mae pwyntiau fel arfer yn cael eu dileu ar ôl 24 mis, ond gall fod yn hirach os yw’n parhau i gyflwyno’n hwyr. Os bydd yn cyrraedd $n o bwyntiau, bydd angen iddo dalu cosb o £200."
      case false => //Individual
        n => s"Mae pwyntiau fel arfer yn cael eu dileu ar ôl 24 mis, ond gall fod yn hirach os ydych yn parhau i anfon cyflwyniadau’n hwyr. Os ydych yn cyrraedd $n o bwyntiau, bydd angen i chi dalu cosb o £200."
    }
    override val pointsAccruingWarning: Boolean => String = {
      case true => //Agent
        "! Warning Bydd eich cleient yn cael cosb o £200 os bydd yn gwneud cyflwyniad arall yn hwyr."
      case false => //Individual
        "! Warning Bydd cosb o £200 yn cael ei chodi arnoch os byddwch yn anfon cyflwyniad arall yn hwyr."
    }

    //Messages when points == threshold (1 penalty triggered)
    override val penaltyP1: Boolean => String = {
      case true => //Agent
        "Codir cosb ychwanegol o £200 ar eich cleient bob tro y bydd yn anfon cyflwyniad hwyr yn y dyfodol, hyd nes y bydd pob un o’i bwyntiau wedi’u dileu. Dylai’ch cleient anfon unrhyw gyflwyniadau sy’n hwyr cyn gynted â phosibl os nad yw eisoes wedi gwneud hynny."
      case false => //Individual
        "Codir cosb ychwanegol o £200 arnoch bob tro y byddwch yn anfon cyflwyniad hwyr yn y dyfodol, hyd nes y bydd pob un o’ch pwyntiau wedi’u dileu. Dylech anfon unrhyw gyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
    }
    override val penaltyWarning: Boolean => String = {
      case true => //Agent
        "! Warning Codwyd cosb o £200 ar eich cleient oherwydd ei fod wedi cyrraedd y trothwy ar gyfer pwyntiau cosb."
      case false => //Individual
        "! Warning Codwyd cosb o £200 arnoch oherwydd eich bod wedi cyrraedd y trothwy ar gyfer pwyntiau."
    }

    //Messages when points == threshold (more than one financial penalty triggered)
    override val additionalPenaltyP1: Boolean => String = {
      case true => //Agent
        "Codir cosb bellach o £200 arno bob tro y bydd yn anfon cyflwyniad yn hwyr yn y dyfodol, hyd nes y bydd pob un o’i bwyntiau wedi’u dileu. Dylai’ch cleient anfon unrhyw gyflwyniadau sy’n hwyr cyn gynted â phosibl os nad yw eisoes wedi gwneud hynny."
      case false => //Individual
        "Codir cosb ychwanegol o £200 arnoch bob tro y byddwch yn anfon cyflwyniad yn hwyr yn y dyfodol, hyd nes y bydd pob un o’ch pwyntiau wedi’u dileu. Dylech anfon unrhyw gyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
    }
    override val additionalPenaltyWarning: Boolean => String = {
      case true => //Agent
        "! Warning Codwyd cosb ychwanegol o £200 ar eich cleient."
      case false => //Individual
        "! Warning Codwyd cosb ychwanegol o £200 arnoch."
    }

    override val pointsGuidanceLink = "Darllenwch yr arweiniad am gosbau am dalu’n hwyr"
    override val addedPointsGuidanceLink = "Read the guidance about adjustment points (Welsh)"
    override val actionsLink: Boolean => String = {
      case true =>
        "Y camau y mae’n rhaid i’ch cleient eu cymryd i gael ei bwyntiau wedi’u dileu erbyn mis Ebrill 2028"
      case false =>
        "Y camau i’w cymryd i gael eich pwyntiau wedi’u dileu erbyn mis Ebrill 2028"
    }
  }
}
