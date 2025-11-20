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

object PenaltyTagStatusMessages {

  sealed trait Messages { this: i18n =>
    val active: String = "Active"
    val expired: String = "Expired"
    val removed: String = "Removed"
    val upheld: String = "Decision upheld"
    val due: String = "Due"
    val overdue: String = "Overdue"
    val amountDue: String => String = amount => s"£$amount due"
    val paid = "Paid"
    val estimate = "Estimate"
    val cancelled = "Cancelled"
    val breathingSpace = "Breathing Space"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val active: String = "Ar waith"
    override val expired: String = "Wedi dod i ben"
    override val removed: String = "Wedi’i dynnu"
    override val upheld: String = "Penderfyniad wedi’i gadarnhau"

    override val due: String = "Yn ddyledus"
    override val overdue: String = "Yn hwyr"
    override val amountDue: String => String = amount => s"£$amount yn ddyledus"
    override val paid = "Wedi’i dalu"
    override val estimate = "Amcangyfrif"
    override val cancelled = "Wedi canslo"
    override val breathingSpace = "Amser i gael eich gwynt atoch"
  }
}
