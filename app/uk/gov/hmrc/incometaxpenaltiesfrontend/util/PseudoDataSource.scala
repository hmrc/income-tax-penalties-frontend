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

import play.api.libs.json.{JsArray, Json}

import scala.util.Random

object PseudoDataSource {
  private val random = new Random(0)

  private def from(chars: String): LazyList[Char] = LazyList continually (chars.charAt(random nextInt chars.length))

  private def hex: LazyList[Char] = from("abcdef0123456789")
  private def numeric: LazyList[Char] = from("0123456789")
  private def alpha: LazyList[Char] = from("abcdefghijklmnopqrstuvqwxyz")

  private def reference: LazyList[String] = LazyList continually Seq(8,4,4,4,12).map(hex.take(_).mkString).mkString("-")

  private def nino: LazyList[String] = LazyList continually Seq(from("ABCEGHJKLMNOPRSTWXYZ").take(1).mkString, from("ABCEGHJKLMNPRSTWXYZ").take(1).mkString, numeric.take(6).mkString, from("ABCD").take(1).mkString).mkString

  private def appealId: LazyList[String] = LazyList continually hex.take(16).mkString.toUpperCase

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

  private def status: LazyList[String] = LazyList continually statii(random nextInt statii.length)

  //def dateTime: LazyList[LocalDateTime] = LazyList continually LocalDateTime. (random.between(ofYearDay(2023, 1).toEpochDay, ofYearDay(2024, 1).toEpochDay))

  // NOTE: currently misusing the "properties" array to hold nino and appeal ID for demo purposes to
  private[util] def submission = s"""{
    |  "reference": "${reference.head}",
    |  "status": "${status.head}",
    |  "numberOfAttempts": ${random.nextInt(100)},
    |  "createdAt": "2024-06-11T12:00:00Z",
    |  "updatedAt": "2024-06-12T00:00:00Z",
    |  "nextAttemptAt": "2024-06-12T12:00:00Z",
    |  "notification": {
    |    "informationType": "foo",
    |    "file": {
    |      "recipientOrSender": "recipient1",
    |      "name": "file1.txt",
    |      "location": "http://example.com/file1.txt",
    |      "checksum": {
    |        "algorithm": "SHA-256",
    |        "value": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    |      },
    |      "size": ${random.nextInt(100000)},
    |      "properties": [
    |        { "name": "nino", "value": "${nino.head}" },
    |        { "name": "appealId", "value": "${appealId.head}" }
    |      ]
    |    },
    |    "audit": {
    |      "correlationID": "${reference.head}"
    |    }
    |  }
    |}""".stripMargin

  lazy val submissions: JsArray = JsArray((1 to 100).map { _ =>
    Json.parse(submission)
  })
}
