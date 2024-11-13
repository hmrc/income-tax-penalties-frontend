import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.5.0"
  private val playVersion = "play-30"

  val compile = Seq(
    "uk.gov.hmrc"    %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"    %% s"play-frontend-hmrc-$playVersion" % "11.2.0"
  )

  val test = Seq(
    "org.jsoup" % "jsoup" % "1.13.1" % Test,
    "uk.gov.hmrc"     %%   "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  val it = Seq.empty
}
