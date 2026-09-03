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
      case 1 => "You have 1 penalty point for missing a submission deadline. You must send the missing submission as soon as possible if you have not already."
      case n => s"You have $n penalty points for missing submission deadlines. You must send the missing submissions as soon as possible if you have not already."
    }

    val pointsAccruingP2: String = "You’ll get another point if you miss a submission deadline again."
    val pointsAccruingP3: Int => String = threshold => s"Points usually last for 24 months. They can last longer if you keep missing submission deadlines. If you get $threshold points, you’ll have to pay a £200 penalty."
    val pointsAccruingP3Special : String = "Points usually last for 24 months. They can last longer if you keep missing submission deadlines."
    val pointsAccruingWarning: Int => String = threshold =>  s"! Warning If you get $threshold points, you’ll have to pay a £200 penalty"

    //Messages when points == threshold (1 financial penalty triggered)
    val penaltyP1: String = "You’ll get another £200 penalty every time you miss a submission deadline until your penalty points are removed."
    val penaltyP2: String = "You must send the missing submissions as soon as possible if you have not already."
    val penaltyWarning: String = "! Warning You have a £200 penalty to pay because you have 4 penalty points."

    //Messages when points == threshold (more than one financial penalty triggered)
    val additionalPenaltyP1: String = "You’ll get another £200 penalty every time you miss a submission deadline until your penalty points are removed."
    val additionalPenaltyWarning: String = "! Warning You have another £200 penalty to pay"
    val pointsGuidanceLink = "Find out more about late submission penalties"
    val findOutMoreWhenAdjusted = "Find out more about:"
    val lateSubmissionPenaltiesLink = "late submission penalties"
    val addedPointsGuidanceLink = "adjustment points"
    val actionsLink: String = "Find out how to get your penalty points removed"
  }
  
  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val pointsTotal: Int => String = n => s"Cyfanswm pwyntiau cosb: $n"

    //Messages when points are accruing < threshold
    override val pointsAccruingP1: Int => String = {
      case 1 => "Mae gennych 1 pwynt cosb am fethu terfyn amser cyflwyno. Mae’n rhaid i chi anfon y cyflwyniad sydd ar goll cyn gynted â phosibl os nad ydych wedi gwneud hynny eisoes."
      case n => s"Mae gennych $n bwynt cosb am fethu terfynau amser cyflwyno. Mae’n rhaid i chi anfon y cyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych wedi gwneud hynny eisoes."
    }
    
    override val pointsAccruingP2: String = "Byddwch yn cael pwynt arall os byddwch yn methu terfyn amser cyflwyno eto."

    override val pointsAccruingP3: Int => String = threshold => s"Mae pwyntiau fel arfer yn para am 24 mis. Gallant barhau’n hirach os byddwch yn parhau i fethu terfynau amser cyflwyno. Os byddwch yn cael $threshold pwynt, bydd angen i chi dalu cosb o £200."
    override val pointsAccruingP3Special : String = "Mae pwyntiau fel arfer yn para am 24 mis. Gallant barhau’n hirach os byddwch yn parhau i fethu terfynau amser cyflwyno."
    
    override val pointsAccruingWarning: Int => String = threshold => s"! Warning Os byddwch yn cael $threshold pwynt, bydd angen i chi dalu cosb o £200."
    
    //Messages when points == threshold (1 penalty triggered)
    override val penaltyP1: String = "Byddwch yn cael cosb arall o £200 bob tro y byddwch yn methu terfyn amser cyflwyno nes bod eich pwyntiau cosb yn cael eu dileu."
    override val penaltyP2: String = "Mae’n rhaid i chi anfon y cyflwyniadau sydd ar goll cyn gynted â phosibl os nad ydych wedi gwneud hynny eisoes."
    override val penaltyWarning: String = "! Warning Mae gennych gosb o £200 i’w thalu oherwydd fod gennych 4 pwynt cosb."

    //Messages when points == threshold (more than one financial penalty triggered)
    override val additionalPenaltyP1: String = "Byddwch yn cael cosb arall o £200 bob tro y byddwch yn methu terfyn amser cyflwyno nes bod eich pwyntiau cosb yn cael eu dileu."
    override val additionalPenaltyWarning: String = "! Warning Mae gennych gosb arall o £200 i’w thalu"
    override val pointsGuidanceLink = "Dysgwch ragor am gosbau am dalu’n hwyr"
    override  val findOutMoreWhenAdjusted = "Darganfyddwch fwy am:"
    override val lateSubmissionPenaltiesLink = "cosbau am gyflwyno’n hwyr"
    override val addedPointsGuidanceLink = "adjustment points(WELSH)"
    override val actionsLink: String = "Dysgwch sut i gael gwared ar eich pwyntiau cosb"
  }
}
