import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.3"

lazy val microservice = Project("income-tax-penalties-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
//    scalacOptions += "-Wconf:src=routes/.*:s",
//    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions -= "-deprecation", //XXX: remove when Scala 3.3.4
    scalacOptions -= "-unchecked", //XXX: remove when Scala 3.3.4
    scalacOptions -= "-encoding", //XXX: remove when Scala 3.3.4
    PlayKeys.playDefaultPort := 9185,
    pipelineStages := Seq(gzip),
    TwirlKeys.templateImports ++= Seq(
//      "play.twirl.api.HtmlFormat",
//      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "uk.gov.hmrc.govukfrontend.views.viewmodels._",
//      "models.Mode",
//      "controllers.routes._",
//      "viewmodels.govuk.all._"
      "views.ViewUtils._"
    )
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings *)
  .settings(scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")))

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.it,
    scalacOptions -= "-deprecation", //XXX: remove when Scala 3.3.4
    scalacOptions -= "-unchecked", //XXX: remove when Scala 3.3.4
    scalacOptions -= "-encoding", //XXX: remove when Scala 3.3.4
  )
  .settings(scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")))
