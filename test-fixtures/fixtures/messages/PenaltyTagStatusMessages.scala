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

  sealed trait Messages { _: i18n =>
    val active: String = "Active"
    val expired: String = "Expired"
    val upheld: String = "Upheld"
    val due: String = "Due"
    val amountDue: String => String = amount => s"£$amount $due"
    val paid = "Paid"
    val estimate = "Estimate"
  }

  object English extends Messages with En

  object Welsh extends Messages with Cy {
    override val active: String = "AR WAITH"
    override val expired: String = "WEDI DOD I BEN"
    override val upheld: String = "UPHELD (Welsh)"
    override val due: String = "YN DDYLEDUS"
    override val amountDue: String => String = amount => s"£$amount $due"
    override val paid = "WEDI’I DALU"
    override val estimate = "AMCANGYFRIF"
  }
}
