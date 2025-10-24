import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.3.0"
  private val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"    %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"    %% s"play-frontend-hmrc-$playVersion" % "12.19.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.jsoup"       %    "jsoup"                  % "1.21.2"          % Test,
    "uk.gov.hmrc"     %%   "bootstrap-test-play-30" % bootstrapVersion  % Test,
    "org.scalamock"   %% "scalamock"                % "7.5.0"                   % Test,
  )

  val it: Seq[Nothing] = Seq.empty
}
