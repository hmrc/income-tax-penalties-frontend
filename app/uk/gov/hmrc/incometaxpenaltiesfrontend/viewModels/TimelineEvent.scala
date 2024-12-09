package uk.gov.hmrc.incometaxpenaltiesfrontend.viewModels

case class TimelineEvent(headerContent: String,
                         spanContent: String,
                         tagContent: Option[String] = None)
