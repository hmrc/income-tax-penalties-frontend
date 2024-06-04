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

package uk.gov.hmrc.incometaxpenaltiesfrontend.controllers

import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import play.api.i18n.MessagesApi
import play.api.mvc
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc.{Action, ActionBuilder, AnyContent, BodyParser, EssentialAction, MessagesBaseController, MessagesControllerComponents, MessagesRequest, MessagesRequestHeader, PreferredMessagesProvider, Request, Result, WrappedRequest}
import uk.gov.hmrc.incometaxpenaltiesfrontend.PageNavigation
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.internalauth.client.{AuthenticatedRequest, AuthorizationToken, FrontendAuthComponents, IAAction, Resource, ResourceLocation, ResourceType, Retrieval}
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html._
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.HelloWorldPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html.HelloWorldPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.routing.Router
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxpenaltiesfrontend._
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig

import java.time.LocalDate.ofYearDay
import java.time.{LocalDate, LocalDateTime}
import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.Random

//class MessagesRequest2[+A,R](
//  request: Request[A],
//  headerCarrier     : HeaderCarrier,
//  authorizationToken: AuthorizationToken,
//  retrieval         : R,
//  val messagesApi: MessagesApi
//) extends AuthenticatedRequest(request, headerCarrier, authorizationToken, retrieval)
//    with PreferredMessagesProvider
//    with MessagesRequestHeader

@Singleton
class AdminController @Inject()(
   messagesControllerComponents: MessagesControllerComponents,
   appConfig: AppConfig,
   auth: FrontendAuthComponents,
   indexPage: IndexPage, submissionPage: SubmissionPage
)(implicit ec: ExecutionContext)
  extends FrontendController(messagesControllerComponents) with MessagesBaseController {
  import appConfig._

  private val read = IAAction("READ")

  private def authorised(location: String, action: IAAction)/*(implicit request: Request[_])*/: ActionBuilder[MessagesRequest, AnyContent] = {
    messagesControllerComponents.messagesActionBuilder.compose(
      auth.authorizedAction(
        continueUrl = routes.HelloWorldController.helloWorld,
        predicate = Permission(
          Resource(
            ResourceType(appName),
            ResourceLocation(location)
          ),
          action
        ),
        retrieval = Retrieval.username
      )
    )
    Action
  }

  private def canonicallyAuthorised()(implicit ec: ExecutionContext): ActionBuilder[MessagesRequest, AnyContent] = {
    val underlyingAction = Action
    new ActionBuilder[MessagesRequest, AnyContent]() {
      override def parser: BodyParser[AnyContent] = underlyingAction.parser
      override def invokeBlock[A](request: Request[A], block: MessagesRequest[A] => Future[Result]): Future[Result] = {
        underlyingAction.invokeBlock(request, block)
      }
      override protected def executionContext: ExecutionContext = ec
    }
  }

  import uk.gov.hmrc.incometaxpenaltiesfrontend.util.PseudoDataSource._

  val tableHeader: Seq[String] = Seq("Reference", "Status", "Attempt #","Created At","Updated At","Next Attempt At")
  val tableData: Seq[Seq[String]] = submissions.map { _.toSeq }

  def index[A](): EssentialAction = canonicallyAuthorised().async { implicit request =>
    val navigation: PageNavigation = service.index
    val foo: HtmlFormat.Appendable = indexPage(navigation, tableHeader, tableData)
    Future.successful(Ok(foo))
  }

  def submission(reference: String): Action[AnyContent] = Action.async { implicit request =>
    val route = routes.HelloWorldController.helloWorld
    val navigation: PageNavigation = appConfig.service :+ ("Home", route) called "Submission Log"
    submissions.find(_.reference==reference) match {
      case Some(data) =>
        val foo: HtmlFormat.Appendable = submissionPage(navigation, data.toSeq)
        Future.successful(Ok(foo))
      case None =>
        Future.successful(NotFound)
    }
  }

//  def index(): Action[AnyContent] = Action.async { implicit request =>
//    val route = routes.HelloWorldController.helloWorld
//    val navigation = service index
//    //Future.successful(Ok(index(navigation)))
//    Future.successful(Ok())
//  }
//
//  def index(): Action[AnyContent] = Action.async { implicit request =>
//    val route = routes.HelloWorldController.helloWorld
//    val navigation = service called ""
//
//
//    // Future.successful(Ok(hmrcInternalPage(appConfig.service)(Html(""))))
//  }
//
//  def submissions(): Action[AnyContent] = Action.async { implicit request =>
//    val route = routes.HelloWorldController.helloWorld
//    //val navigation = appConfig.service :+ (route)
//
//    ???
//    //Future.successful(Ok(helloWorldPage(appConfig.service)))
//  }

}