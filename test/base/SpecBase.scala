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

package base

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.spi.ILoggingEvent
import com.google.inject
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.{Binder, Key}
import controllers.actions.*
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must
import org.scalatest.{OptionValues, TryValues}
import play.api.{Application, Configuration, Environment}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.{BinderOption, GuiceApplicationBuilder, GuiceableModule}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.reflect.{ClassTag, classTag}

trait SpecBase
  extends AnyFreeSpec
    with must.Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with LogCapturing {

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction]
      ).configure(
        "pta-account-menu.account-home.href" -> "#",
        "pta-account-menu.messages.href" -> "#",
        "pta-account-menu.check-progress.href" -> "#",
        "pta-account-menu.your-profile.href" -> "#",
        "pta-account-menu.business-tax-account.href" -> "#"
      )

  implicit class StringEx(s: String) {
    def deNonce: String = s.replaceAll("""nonce="[^"]*"""", "")
  }

  implicit class LogEventEx(log: Seq[ILoggingEvent]) {
    def messages: Seq[(Level, String)] = log.filter(_.getLevel.isGreaterOrEqual(INFO)).map(m => m.getLevel -> m.getMessage)
  }

  def bindInstance[T](clazz: Class[T], instance: T): GuiceableModule = {
    object Bar extends inject.Module {
      override def configure(binder: Binder): Unit = {
        binder.bind(Key.get(clazz)).toInstance(instance)
        binder
      }
    }

    new GuiceableModule {
      override def guiced(env: Environment, conf: Configuration, binderOptions: Set[BinderOption]): Seq[inject.Module] = Seq(Bar)
      override def disable(classes: Seq[Class[_]]): GuiceableModule = throw new NotImplementedError()
    }
  }

  def bindInstance[T: ClassTag](instance: T): GuiceableModule = bindInstance(classTag[T].runtimeClass.asInstanceOf[Class[T]], instance)
}
