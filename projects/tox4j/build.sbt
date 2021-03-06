// General settings.
organization  := "im.tox"
name          := "tox4j"
scalaVersion  := "2.11.6"

// Enable the plugins we want.
import ScoverageSbtPlugin.ScoverageKeys._
import sbt.tox4j._
import sbt.tox4j.lint._

// Build plugins.
Assembly.moduleSettings
Benchmarking.projectSettings
CodeFormat.projectSettings
Jni.moduleSettings
ProtobufJni.moduleSettings

/******************************************************************************
 * Dependencies
 ******************************************************************************/

// Snapshot and linter repository.
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// Build dependencies.
libraryDependencies ++= Seq(
  "com.intellij" % "annotations" % "12.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.json" % "json" % "20131018",
  "org.scalaz" %% "scalaz-core" % "7.2.0-M1",
  "org.joda" % "joda-convert" % "1.6",
  "joda-time" % "joda-time" % "2.3"
)

// Test dependencies.
libraryDependencies ++= Seq(
  "com.intellij" % "forms_rt" % "7.0.3",
  "com.storm-enroute" %% "scalameter" % "0.7-SNAPSHOT",
  "junit" % "junit" % "4.12",
  "org.scalacheck" %% "scalacheck" % "1.12.2",
  "org.scalatest" %% "scalatest" % "2.2.4",
  "org.slf4j" % "slf4j-log4j12" % "1.7.12"
) map (_ % Test)

// Add ScalaMeter as test framework.
testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

// JNI.
import sbt.tox4j.Jni.Keys._

packageDependencies ++= Seq(
  "protobuf-lite",
  "libtoxcore",
  "libtoxav",
  // Required, since toxav's pkg-config files are incomplete:
  "libsodium",
  "vpx"
)

// TODO: infer this (easy).
jniSourceFiles in Compile ++= Seq(
  managedNativeSource.value / "Av.pb.cc",
  managedNativeSource.value / "Core.pb.cc"
)

/******************************************************************************
 * Other settings and plugin configuration.
 ******************************************************************************/

// Mixed project.
compileOrder := CompileOrder.Mixed
scalaSource in Compile := (javaSource in Compile).value
scalaSource in Test    := (javaSource in Test   ).value

// Lint plugins.
Checkstyle.moduleSettings
Findbugs.moduleSettings
Foursquare.moduleSettings
Scalastyle.moduleSettings

// Local overrides for linters.
WartRemoverOverrides.moduleSettings

// TODO(iphydf): Require less test coverage for now, until ToxAv is tested.
Coverage.projectSettings
coverageMinimum := 78

// Tox4j-specific style checkers.
addCompilerPlugin("im.tox" %% "linters" % "0.1-SNAPSHOT")

// Override Scalastyle configuration for test.
scalastyleConfigUrl in Test := None
scalastyleConfig in Test := (scalaSource in Test).value / "scalastyle-config.xml"
