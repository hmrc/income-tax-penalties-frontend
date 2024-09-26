/*
 * Copyright 2023 HM Revenue & Customs
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

package utils

import org.scalatest.matchers.should
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import utils.FuturePartition.*

import scala.concurrent.Future.successful
import scala.concurrent.{Future, Promise}
import scala.util.{Success, Try}

class FuturePartitionSpec extends AsyncWordSpec with should.Matchers with MockitoSugar {

  val neverFoo = Promise[String].future
  val failedFoobar = Future.failed(new Exception("foobar"))

  "race" should {
    val successFoo = successful("foo")
    val successBar = successful("bar")

    "throw NoSuchElementException if sequence is empty" in {
      val tryFuture = Try(race())
      tryFuture.isFailure shouldBe true
      tryFuture.failed.get.getClass shouldBe classOf[NoSuchElementException]
    }

    "return Future.never if sequence never completes" in {
      val future = race(neverFoo)
      Future {
        future.isCompleted shouldBe false
      }
    }

    "return result if successful" in {
      val future = race(successful("foo"))
      future.map { result =>
        result.head shouldBe Success("foo")
        result.tail shouldBe Seq.empty
      }
    }

    "return first result of many successful ones" in {
      val future = race(successFoo, successBar)
      future.map { result =>
        result.head shouldBe Success("foo")
        result.tail shouldBe List(successBar)
      }
    }

    "return second result if first is late" in {
      val future = race(neverFoo, successBar)
      future.map { result =>
        result.head shouldBe Success("bar")
        result.tail shouldBe List(neverFoo)
      }
    }

    "return first result even if the second would fail" in {
      val future = race(successFoo, failedFoobar)
      future.map { result =>
        result.head shouldBe Success("foo")
        result.tail shouldBe List(failedFoobar)
      }
    }

    "return second result if first has failed" in {
      val future = race(failedFoobar, successBar)
      future.map { result =>
        result.head.isFailure shouldBe true
        result.head.failed.get.getMessage shouldBe "foobar"
        result.tail shouldBe List(successBar)
      }
    }
  }

  "matching" should {
    "return x" in {
      val partition = successful(FuturePartition(Success("ok1"), Seq(successful("no"), successful("ok2"))))
      val future: Future[FuturePartition[String]] = partition.matching(_.startsWith("ok"))
      for (
        result <- future;
        tail <- result.next
      ) yield {
        result.head shouldBe Success("ok1")
        tail.head shouldBe Success("ok2")
      }
    }
  }

  "take" should {
    "return first n values in partition" in {
      val partition = successful(FuturePartition(Success("A"), Seq(successful("B"),successful("C"),failedFoobar)))
      val future: Future[Seq[String]] = partition.take(3)
      future.map { result =>
        result shouldBe Seq("A","B","C")
      }
    }
  }


  "firstNonEmpty" should {
    "return first non-empty option in partition" in {
      val partition: Future[FuturePartition[Option[String]]] = successful(FuturePartition(Success(None), Seq(successful(None), successful(Some("foo")))))
      val future: Future[String] = partition.firstNonEmpty
      future.map { result =>
        result shouldBe "foo"
      }
    }
  }

}