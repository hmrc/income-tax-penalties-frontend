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

  sealed trait Messages {
    this: i18n =>
    val pointsTotal: Int => String = n => s"Penalty points total: $n"

    //Messages when points are accruing < threshold
    val pointsAccruingP1: Int => String = {
      case 1 => "You have 1 penalty point for sending a late submission. You should send this missing submission as soon as possible if you haven’t already."
      case n => s"You have $n penalty points for sending late submissions. You should send any missing submissions as soon as possible if you haven’t already."
    }

    val pointsAccruingP2: String = "You’ll get another point if you send another submission after a deadline has passed."

    val pointsAccruingP3: Int => String = {
      n => s"Points usually expire after 24 months, but it can be longer if you keep sending late submissions. If you reach $n points, you’ll have to pay a £200 penalty."
    }

    val pointsAccruingWarning: String = "! Warning You’ll get a £200 penalty if you send another late submission."

    //Messages when points == threshold (1 financial penalty triggered)
    val penaltyP1: String = "You will get an additional £200 penalty every time you send a late submission in the future, until your points are removed. You should send any missing submissions as soon as possible if you haven’t already."
    val penaltyWarning: String = "! Warning You have been given a £200 penalty for reaching the penalty threshold."

    //Messages when points == threshold (more than one financial penalty triggered)
    val additionalPenaltyP1: String = "You will get another £200 penalty every time you send a late submission in the future, until your points are removed. You should send any missing submissions as soon as possible if you haven’t already."
    val additionalPenaltyWarning: String = "! Warning You have been given an additional £200 penalty."
    val pointsGuidanceLink = "Read the guidance about late submission penalties"
    val addedPointsGuidanceLink = "Read the guidance about adjustment points"
    val actionsLink: String = "Actions you must take to get your points removed by April 2028"
  }
  
  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val pointsTotal: Int => String = n => s"Cyfanswm pwyntiau cosb: $n"

    //Messages when points are accruing < threshold
    override val pointsAccruingP1: Int => String = {
      case 1 => "Mae gennych 1 pwynt cosb am gyflwyno’n hwyr. Dylech anfon y cyflwyniad sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
      case n => s"Mae gennych $n o bwyntiau cosb am gyflwyno’n hwyr. Dylech anfon unrhyw gyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
    }
    
    override val pointsAccruingP2: String = "Byddwch yn cael pwynt arall os byddwch yn anfon cyflwyniad arall ar ôl i’r dyddiad cau fynd heibio."
    
    override val pointsAccruingP3: Int => String = {
        n => s"Mae pwyntiau fel arfer yn cael eu dileu ar ôl 24 mis, ond gall fod yn hirach os ydych yn parhau i anfon cyflwyniadau’n hwyr. Os ydych yn cyrraedd $n o bwyntiau, bydd angen i chi dalu cosb o £200."
    }
    
    override val pointsAccruingWarning: String = "! Warning Bydd cosb o £200 yn cael ei chodi arnoch os byddwch yn anfon cyflwyniad arall yn hwyr."
    
    //Messages when points == threshold (1 penalty triggered)
    override val penaltyP1: String = "Codir cosb ychwanegol o £200 arnoch bob tro y byddwch yn anfon cyflwyniad hwyr yn y dyfodol, hyd nes y bydd pob un o’ch pwyntiau wedi’u dileu. Dylech anfon unrhyw gyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
    override val penaltyWarning: String = "! Warning Codwyd cosb o £200 arnoch oherwydd eich bod wedi cyrraedd y trothwy ar gyfer pwyntiau."

    //Messages when points == threshold (more than one financial penalty triggered)
    override val additionalPenaltyP1: String = "Codir cosb ychwanegol o £200 arnoch bob tro y byddwch yn anfon cyflwyniad yn hwyr yn y dyfodol, hyd nes y bydd pob un o’ch pwyntiau wedi’u dileu. Dylech anfon unrhyw gyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych eisoes wedi gwneud hynny."
    override val additionalPenaltyWarning: String = "! Warning Codwyd cosb ychwanegol o £200 arnoch."
    override val pointsGuidanceLink = "Darllenwch yr arweiniad am gosbau am dalu’n hwyr"
    override val addedPointsGuidanceLink = "Darllenwch yr arweiniad ynghylch pwyntiau addasu"
    override val actionsLink: String = "Y camau i’w cymryd i gael eich pwyntiau wedi’u dileu erbyn mis Ebrill 2028"
  }
}
