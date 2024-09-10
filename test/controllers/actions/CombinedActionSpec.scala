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

package controllers.actions

import base.SpecBase
import controllers.agent.SessionKeys
import controllers.agent.SessionKeys.clientMTDID
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.{ActionBuilder, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class CombinedActionSpec extends SpecBase {

  class Setup {
    val bodyParser = mock[BodyParsers.Default]
    val agentAction = mock[AgentAction]
    val individualAction = mock[IdentifierAction]

    val combinedActionBuilder = new CombinedAction(individualAction, agentAction, bodyParser).apply()
    val combinedAction = combinedActionBuilder { (_: IdentifierRequest[AnyContent]) => Results.NoContent }
  }

  "Combined Action" - {
    "delegates individuals to the individual auth action" in new Setup {
        combinedAction(FakeRequest())

        verify(individualAction).invokeBlock(any(), any())
        verifyNoMoreInteractions(agentAction, individualAction)
    }

    "delegates agents to the agent auth action" in new Setup {
      val actionBuilder = mock[ActionBuilder[_,_]]
      when(agentAction.apply("foo")).thenReturn(actionBuilder)

      combinedAction(FakeRequest().withSession(clientMTDID -> "foo"))

      verify(agentAction).apply("foo")
      verify(actionBuilder).invokeBlock(any(), any())
      verifyNoMoreInteractions(agentAction, individualAction, actionBuilder)
    }
  }
}
