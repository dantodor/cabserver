import sbt._
import Keys._


lazy val buildSettings = Seq(
  version       := "0.0.1",
  scalaVersion  := "2.12.1",
  organization := "com.test",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:higherKinds", "-language:implicitConversions", "-Ydelambdafy:method", "-target:jvm-1.8"),
  resolvers     ++= Seq(
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("hseeberger", "maven")
  )
)

val akkaV      = "2.4.17"
val akkaHttpV  = "10.0.4"
val akkaCors   = "0.1.11"
val circeV     = "0.7.0"
val akkaCirceV = "1.12.0"
val scalaTestV = "3.0.1"
val logbackV   = "1.2.1"


lazy val `template` = project
  .in(file("."))
  .settings(buildSettings: _*)
  .settings(mainClass in assembly := Some("com.test.server.HttpServer"))
  .settings(
    name := "httpserver",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpV,
      "com.typesafe.akka" %% "akka-slf4j"        % akkaV,
      "de.heikoseeberger" %% "akka-http-circe"   % akkaCirceV,
      "ch.megard"         %% "akka-http-cors"    % akkaCors,
      "ch.qos.logback"    % "logback-classic"    % logbackV,
      "io.circe"          %% "circe-core"        % circeV,
      "io.circe"          %% "circe-generic"     % circeV,
      "io.circe"          %% "circe-jawn"        % circeV,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV  % "test",
      "org.scalatest"     %% "scalatest"         % scalaTestV % "test"
    )
  )
//  .enablePlugins(AutomateHeaderPlugin)


parallelExecution in Test := false
fork in Test := true
