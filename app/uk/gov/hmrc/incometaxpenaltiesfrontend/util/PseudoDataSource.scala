/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxpenaltiesfrontend.util

import uk.gov.hmrc.incometaxpenaltiesfrontend.util.PseudoDataSource.status

import java.time.LocalDateTime
import scala.util.Random

object PseudoDataSource {
  val random = new Random(0)

  private val hexChars = "abcdef0123456789"
  def hex: LazyList[Char] = LazyList continually (hexChars charAt (random nextInt hexChars.length))

  def reference: LazyList[String] = LazyList continually Seq(8,4,4,4,12).map(hex.take(_).mkString).mkString("-")

  val statii = Seq(
    "PENDING",
    "SENT",
    "FILE_RECEIVED_IN_SDES",
    "FILE_NOT_RECEIVED_IN_SDES_PENDING_RETRY",
    "FILE_PROCESSED_IN_SDES",
    "FAILED_PENDING_RETRY",
    "NOT_PROCESSED_PENDING_RETRY",
    "PERMANENT_FAILURE"
  )

  def status: LazyList[String] = LazyList continually statii(random nextInt statii.length)

  //def dateTime: LazyList[LocalDateTime] = LazyList continually LocalDateTime. (random.between(ofYearDay(2023, 1).toEpochDay, ofYearDay(2024, 1).toEpochDay))

  case class Submission(
    reference: String = PseudoDataSource.reference.head,
    status: String = PseudoDataSource.status.head,
    retryCount: Int = random.nextInt(100),
    createdAt: String = "2023-01-01 00:00:00",
    updatedAt: String = "2023-01-01 00:00:00",
    nextAttemptAt: String = "2023-01-01 00:00:00"
  ) {
    def toSeq: Seq[String] = Seq(s"<a href=submission/${reference}>${reference}</a>", status, retryCount.toString, createdAt, updatedAt, nextAttemptAt)
  }

  val submissions: Seq[Submission] = (1 to 100).map { _ => new Submission }

}
