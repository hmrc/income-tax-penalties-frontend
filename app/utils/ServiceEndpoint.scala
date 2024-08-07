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

package utils

import play.api.Configuration

import java.net.URL

case class ServiceEndpoint(protocol: String, host: String, port: Option[Int], prefix: String) {
  private def path = Some(prefix).filterNot(_.isBlank).map(_.stripSuffix("/")+"/").getOrElse("")
  private def portSpec = port.map(port => s":$port").getOrElse("")
  private val urlBase = s"$protocol://$host$portSpec/$path"
  def resolve(suffix: String): URL = new URL(urlBase + suffix.stripPrefix("/"))
}
object ServiceEndpoint {
  def apply(cfg: Configuration)(service: String) = new ServiceEndpoint(
    cfg.getOptional[String](s"microservice.services.$service.protocol").getOrElse("http"),
    cfg.getOptional[String](s"microservice.services.$service.host").getOrElse("localhost"),
    cfg.getOptional[Int](s"microservice.services.$service.port"),
    cfg.getOptional[String](s"microservice.services.$service.prefix").getOrElse(service)
  )
}
