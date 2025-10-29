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

object AppealStatusMessages {

  sealed trait Messages { this: i18n =>
    val appealStatusKey = "Appeal status"
    val appealInProgress = "Appeal in progress"
    val appealRejected = "Appeal rejected"
    val appealSuccessful = "Appeal successful"
    val decisionUpheld = "Decision upheld"
    val underReview = "Under review"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val appealStatusKey = "Statws yr apêl"
    override val appealInProgress = "Apêl ar y gweill"
    override val appealRejected = "Apêl wedi’i wrthod"
    override val appealSuccessful = "Apêl lwyddiannus"
    override val decisionUpheld = "Penderfyniad wedi’i gadarnhau"
    override val underReview = "Dan adolygiad"
  }
}
