import sbt.*

object AppDependencies {

  private val playVersion = "play-30"
  private val playPartialsVersion = "9.1.0"
  private val bootstrapVersion = "8.5.0"
  private val hmrcMongoVersion = "1.8.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"             %% s"play-partials-$playVersion"      % playPartialsVersion,
    "uk.gov.hmrc"             %% s"play-frontend-hmrc-$playVersion" % "9.5.0",
    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-$playVersion"         % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion            % Test,
    "org.jsoup"               %  "jsoup"                      % "1.13.1"            % Test,
  )

  val it = Seq.empty
}
