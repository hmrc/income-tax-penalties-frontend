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

import org.apache.commons.lang3.CharSet
import play.api.libs.json.{JsLookupResult, JsObject, JsString, JsValue, Reads}
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.incometaxpenaltiesfrontend.PageNavigation
import uk.gov.hmrc.incometaxpenaltiesfrontend.config.AppConfig
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable
import uk.gov.hmrc.incometaxpenaltiesfrontend.model.InternalTable.TblHead
import uk.gov.hmrc.incometaxpenaltiesfrontend.views.html._
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URLDecoder
import java.util.Comparator
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

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
   indexPage: IndexPage,
   submissionPage: SubmissionPage
)(implicit ec: ExecutionContext)
  extends FrontendController(messagesControllerComponents) with MessagesBaseController {
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

  import uk.gov.hmrc.incometaxpenaltiesfrontend.util.PseudoDataSource._

  val tableHeader = (
    TblHead("Reference", _.\("reference").asOpt[String], {ref: String => s"<a href=submission/$ref>$ref</a>"}),
    TblHead("Status", _.\("status").asOpt[String]),
    TblHead("Attempt #", _.\("numberOfAttempts").asOpt[Int]),
    TblHead("Created At", _.\("createdAt").asOpt[String]),
    TblHead("Updated At", _.\("updatedAt").asOpt[String]),
    TblHead("Next Attempt At", _.\("nextAttemptAt").asOpt[String])
  )

  val table = InternalTable(tableHeader)

  def index[A](sort: Seq[String], filter: Seq[String], page: Option[Int]): EssentialAction = canonicallyAuthorised().async { implicit request =>
    val navigation: PageNavigation = service.index
    val htmlTableHeader: Seq[String] = table.headers.map(_.name)

    val x: Seq[(JsObject,JsObject) => Boolean] = sort.map { spec =>
      spec.split("-", 2) match {
        case Array(direction, fieldName) =>
          val comparator = table.headers.find(_.symbol == fieldName).map(_.comparator)
          (direction, comparator) match {
            case ("asc", Some(comparator)) => { case (l,r) => comparator(l,r) > 0 }
            case ("desc", Some(comparator)) => { case (l,r) => comparator(l,r) <= 0 }
            case (dir, None) => throw new Exception(s"""Bad column name: $fieldName""");
            case (dir, _) => throw new Exception(s"""Bad direction: $dir""");
          }
        case x => throw new Exception(s"""$spec became ${x.mkString(" ")}"""); ???
      }
    }
    def xs(l: JsObject, r: JsObject): Boolean = x.foldLeft(true){ case (a,f) => a && f(l,r) }

    val y: Seq[JsObject => Boolean] = filter.map { spec =>
      URLDecoder.decode(spec, "UTF8").split("=", 2) match {
        case Array(fieldName, filterValue) => table.headers.find(_.name == fieldName); { case i => true }
        case Array(fieldName, filterValue) => table.headers.find(_.name == fieldName); { case i => true }
        case x => throw new Exception(s"""$spec becamse ${x.mkString(" ")}"""); { case i => true }
      }
    }
    def ys(js: JsObject): Boolean = y.foldLeft(true){ case (a,f) => a && f(js) }

    val pageSize = 20

    val pageData = submissions.value.map(_.as[JsObject]).filter(ys).sortWith(xs).grouped(pageSize).drop(page.getOrElse(0)).nextOption()

    pageData match {
      case Some(pageData) =>
        val tableData: Seq[Seq[String]] = pageData.map { case js: JsObject =>
          table.headers.map(_.html(js))
        }.toSeq

        val foo: HtmlFormat.Appendable = indexPage(navigation, htmlTableHeader, tableData)
        Future.successful(Ok(foo))
      case None =>
        Future.successful(Ok("No data"))
    }
  }

  def submission(reference: String): Action[AnyContent] = Action.async { implicit request =>
    val route = routes.AdminController.index(Seq.empty, Seq.empty, None)
    val navigation: PageNavigation = appConfig.service :+ ("Home", route) called "Submission Log"
    submissions.value.find { case js: JsObject =>
      tableHeader._1.lookup(js).exists(_.contains(reference))
    } match {
      case Some(data: JsObject) =>
        val foo: HtmlFormat.Appendable = submissionPage(navigation, table.headers.map(_.html(data)))
        Future.successful(Ok(foo))
      case None =>
        Future.successful(NotFound)
    }
  }

}