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

  sealed trait Messages { _: i18n =>
    val appealStatusKey = "Appeal status"
    val underReviewHMRC = "Under review by HMRC"
    val underReviewTaxTribunal = "Under review by the tax tribunal"
    val acceptedTaxTribunal = "Appeal accepted by tax tribunal"
    val accepted = "Appeal accepted"
    val rejected = "Appeal rejected"
    val rejectedTaxTribunal = "Appeal rejected by tax tribunal"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val appealStatusKey = "Statws yr apêl"
    override val underReviewHMRC = "Under review by HMRC (Welsh)"
    override val underReviewTaxTribunal = "Under review by the tax tribunal (Welsh)"
    override val acceptedTaxTribunal = "Appeal accepted by tax tribunal (Welsh)"
    override val accepted = "Appeal accepted (Welsh)"
    override val rejected = "Apêl wedi’i wrthod"
    override val rejectedTaxTribunal = "Appeal rejected by tax tribunal (Welsh)"
  }
}
