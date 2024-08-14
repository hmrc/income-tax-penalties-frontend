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
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.ExceptionUtils._

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}
import scala.util.Success

class ExceptionUtilsSpec extends AnyWordSpec with should.Matchers with GuiceOneAppPerSuite with MockitoSugar {

  class ExampleThrowable(message: String) extends Throwable(message)

  "Throwable.summary" should {
    "compose a summary of a throwable a with message" in {
      new ExampleThrowable("foo").summary shouldBe "ExampleThrowable: foo"
    }

    "return just the simple class name for a throwable with an empty message" in {
      new ExampleThrowable("").summary shouldBe "ExampleThrowable"
    }

    "return just the simple class name for a throwable with a null message" in {
      new ExampleThrowable(null).summary shouldBe "ExampleThrowable"
    }

    "return \"null\" for a null throwable" in {
      null.asInstanceOf[Throwable].summary shouldBe "null"
    }
  }

  "delayFuture" should {
    val exampleFailure = new ExampleThrowable("bar")

    "pass through a success" in {
      val result = successful("foo").delayFailure
      result.value shouldBe Some(Success("foo"))
    }

    "pass through a failure" in {
      val result = failed(exampleFailure).delayFailure
      result.failed.value shouldBe Some(Success(exampleFailure))
    }

    "convert an immediate success" in {
      def foobar: Future[_] = throw exampleFailure
      val result = foobar.delayFailure
      result.failed.value shouldBe Some(Success(exampleFailure))
    }
  }
}
