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

import org.apache.pekko.actor.ActorSystem
import play.api.Logging
import play.api.libs.json.{JsNumber, JsObject, JsString}
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.incometaxpenaltiesfrontend.PageNavigation
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable.{SimpleDataSource, TblHead}
import uk.gov.hmrc.incometaxpenaltiesfrontend.util.PseudoDataSource.submissions
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html._
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.lang.Math.abs
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class AdminController @Inject()(
   messagesControllerComponents: MessagesControllerComponents,
   appConfig: AppConfig,
   auth: FrontendAuthComponents,
   indexPage: IndexPage,
   submissionPage: SubmissionPage,
   actorSystem: ActorSystem
)(implicit ec: ExecutionContext)
  extends FrontendController(messagesControllerComponents) with MessagesBaseController with Logging {
  import appConfig._

  private val read = IAAction("READ")

  private def authorised(location: String, action: IAAction)/*(implicit request: Request[_])*/: ActionBuilder[MessagesRequest, AnyContent] = {
    messagesControllerComponents.messagesActionBuilder.compose(
      auth.authorizedAction(
        continueUrl = routes.AdminController.index(Seq.empty, Seq.empty, None),
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

  private val tableHeader = (
    TblHead("Reference", _.\("reference").asOpt[String], markup = {ref: String => Html(s"<a href=submission/$ref>$ref</a>")}),
    TblHead("Status", _.\("status").asOpt[String]),
    TblHead("Attempt #", _.\("numberOfAttempts").asOpt[Int]),
    TblHead("Created At", _.\("createdAt").asOpt[String], format = {ref: String => ref.replaceAll("[TZ]", " ")}),
    TblHead("Updated At", _.\("updatedAt").asOpt[String], format = {ref: String => ref.replaceAll("[TZ]", " ")}),
    TblHead("Next Attempt At", _.\("nextAttemptAt").asOpt[String], format = {ref: String => ref.replaceAll("[TZ]", " ")})
  )

  private val table = InternalTable(tableHeader)

  private val DemoDataSource = SimpleDataSource(table, () => submissions.value.map{_.as[JsObject]}.toSeq)

  def index[A](sort: Seq[String], filter: Seq[String], page: Option[Int]): EssentialAction = canonicallyAuthorised().async { implicit request =>
    val route = routes.AdminController.index(Seq.empty, Seq.empty, None)
    if (request.path.endsWith("/")) {
      val navigation: PageNavigation = service.index
      val htmlTableHeader: Seq[String] = table.headers.map(_.name)
      val data = DemoDataSource.pageData(filter, sort, page.getOrElse(0));
      val foo: HtmlFormat.Appendable = indexPage(navigation, htmlTableHeader, data.html)
      Future.successful(Ok(foo))
    } else {
      Future.successful(Redirect(route.copy(url = route.url+"/")))
    }
  }

  def submission(reference: String): Action[AnyContent] = canonicallyAuthorised().async { implicit request =>
    val route = routes.AdminController.index(Seq.empty, Seq.empty, None)
    val navigation: PageNavigation = appConfig.service :+ ("Home", route) called "Submission Log"
    DemoDataSource.find(tableHeader._1.symbol, reference) match {
      case Some(data: JsObject) =>
        val foo: HtmlFormat.Appendable = submissionPage(navigation, reference, table.headers.map(x=>(Html(x.name),x.html(data))))
        Future.successful(Ok(foo))
      case None =>
        Future.successful(NotFound)
    }
  }

  def checkFileUrl(reference: String): Action[AnyContent] = Action.async { implicit request =>
    val randomResponse = {
      abs(reference.hashCode) % 8 match {
        case 0 => Ok(JsObject(Seq("status" -> JsNumber(200), "statusText" -> JsString("Ok"), "type" -> JsString("application/msword"), "size" -> JsNumber(256789))))
        case 1 => Ok(JsObject(Seq("status" -> JsNumber(400), "statusText" -> JsString("Bad Request"))))
        case 2 => Ok(JsObject(Seq("status" -> JsNumber(401), "statusText" -> JsString("Unauthorised"))))
        case 3 => Ok(JsObject(Seq("status" -> JsNumber(403), "statusText" -> JsString("Forbidden"))))
        case 4 => Ok(JsObject(Seq("status" -> JsNumber(404), "statusText" -> JsString("Not Found"))))
        case 5 => Ok(JsObject(Seq("status" -> JsNumber(500), "statusText" -> JsString("Internal Server Error"))))
        case 6 => Ok(JsObject(Seq("status" -> JsNumber(503), "statusText" -> JsString("Service Unavailable"))))
        case 7 => RequestTimeout
      }
    }
    DemoDataSource.find(tableHeader._1.symbol, reference) match {
      case Some(data: JsObject) =>
        val promise: Promise[Result] = Promise()
        new Thread() {
          override def run(): Unit = {
            Thread.sleep(200 + abs(reference.hashCode) % 300)
            promise.success(randomResponse)
          }
        }.start()
        promise.future
      case None =>
        Future.successful(NotFound)
    }
  }

}