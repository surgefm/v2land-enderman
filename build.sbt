import sbtcrossproject.{crossProject, CrossType}

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"

lazy val commonSettings = Seq(
  organization := "org.langchao",
  scalaVersion := "2.12.6",
  version := "0.5.4"
)

lazy val server = (project in file("server")).settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
    "org.mongodb.scala" %% "mongo-scala-driver"   % "2.3.0",

    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
    "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
    "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test,

    "com.github.mauricio" %% "postgresql-async" % "0.2.21",

    "net.ruippeixotog" %% "scala-scraper" % "2.1.0",
    "com.github.wookietreiber" %% "scala-chart" % "latest.integration"
  )
)

lazy val testServer = (project in file("build/test"))
  .settings(
    commonSettings,
    mainClass in Compile := Some("enderman.Main"),
    dockerBaseImage := "openjdk:11.0.1-jre",
    dockerUpdateLatest := true,
    packageName in Docker := "enderman"
  ).enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .dependsOn(server)

lazy val prodServer = (project in file("build/prod"))
  .settings(
    commonSettings,
    mainClass in Compile := Some("enderman.Main"),
    dockerBaseImage := "openjdk:11.0.1-jre",
    dockerUpdateLatest := true,
    packageName in Docker := "enderman"
  ).enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .dependsOn(server)

lazy val shared =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .settings(commonSettings)
    .jvmSettings(
    )
    .jsSettings(
    )

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

