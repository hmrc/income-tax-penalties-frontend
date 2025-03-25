import sbt.Test
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice = Project("income-tax-penalties-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
      libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
      // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
      // suppress warnings in generated routes files
      scalacOptions += "-Wconf:src=routes/.*:s",
      scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
      pipelineStages := Seq(gzip),
      PlayKeys.playDefaultPort := 9185
  )
  .settings(Test/logBuffered := false)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"

  ))
  .settings(inConfig(Test)(testSettings): _*)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  unmanagedSourceDirectories += baseDirectory.value / "test-fixtures",
  Test / javaOptions += "-Dlogger.resource=logback-test.xml",
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings() ++ Seq(
    unmanagedSourceDirectories := Seq(baseDirectory.value / "test-fixtures"),
    Test / javaOptions += "-Dlogger.resource=logback-test.xml"
  ))
  .settings(Test/logBuffered := false)
  .settings(libraryDependencies ++= AppDependencies.it)
