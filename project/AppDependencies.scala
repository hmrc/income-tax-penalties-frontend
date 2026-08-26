import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.8.0"
  private val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"    %% s"play-frontend-hmrc-$playVersion" % "13.11.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.jsoup"       %    "jsoup"                  % "1.23.2"          % Test,
    "uk.gov.hmrc"     %%   "bootstrap-test-play-30" % bootstrapVersion  % Test,
    "org.scalamock"   %%   "scalamock"              % "7.5.5"           % Test,
  )

  val it: Seq[Nothing] = Seq.empty
}
