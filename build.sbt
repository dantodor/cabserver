import sbt._
import Keys._


lazy val buildSettings = Seq(
  version       := "0.0.1",
  scalaVersion  := "2.12.4",
  organization := "com.example",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:higherKinds", 
    "-language:implicitConversions", "-Ydelambdafy:method", "-target:jvm-1.8"),
  resolvers     ++= Seq(
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("hseeberger", "maven")
  )
)

val akkaV      = "2.5.23"
val akkaHttpV  = "10.1.9"
val akkaCors   = "0.4.1"
val circeV     = "0.11.1"
val akkaCirceV = "1.27.0"
val scalaTestV = "3.0.1"
val logbackV   = "1.2.1"


lazy val `cabbie` = project
  .in(file("."))
  .settings(buildSettings: _*)
  .settings(
    name := "cabserver",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpV,
      "com.typesafe.akka" %% "akka-stream"       % akkaV,
      "com.typesafe.akka" %% "akka-slf4j"        % akkaV,
      "de.heikoseeberger" %% "akka-http-circe"   % akkaCirceV,
      "ch.megard"         %% "akka-http-cors"    % akkaCors,
      "ch.qos.logback"    % "logback-classic"    % logbackV,
      "io.circe"          %% "circe-core"        % circeV,
      "io.circe"          %% "circe-generic"     % circeV,
      "io.circe"          %% "circe-jawn"        % circeV,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV  % Test,
      "com.typesafe.akka" %% "akka-testkit"      % akkaV % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaV % Test, 
      "org.scalatest"     %% "scalatest"         % scalaTestV % Test
    )
  )
  .enablePlugins(JavaAppPackaging)

packageName in Docker := "cabserver"
dockerExposedPorts := Seq(8080)

parallelExecution in Test := false
fork in Test := false
