import sbtcrossproject.{crossProject, CrossType}

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"

val sharedSettings = Seq(
  organization := "org.langchao",
  scalaVersion := "2.12.5",
  version := "0.2.13"
)

lazy val org = "org.langchao"
lazy val endermanVer = "0.2.13"
lazy val scalaVer = "2.12.5"

lazy val server = (project in file("server")).settings(
  organization := org,
  scalaVersion := scalaVer,
  version := endermanVer,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
    "org.mongodb.scala" %% "mongo-scala-driver"   % "2.3.0",

    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
    "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
    "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
  ),
  dockerBaseImage := "openjdk:jre-alpine",
  dockerUpdateLatest := true,
  mainClass in Compile := Some("enderman.Main")
).enablePlugins(SbtWeb)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)

lazy val client = (project in file("client")).settings(
  organization := org,
  scalaVersion := scalaVer,
  version := endermanVer,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "com.thoughtworks.binding" %%% "dom" % "latest.release"
  )
).enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSWeb)


lazy val enderman =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Full)
    .settings(sharedSettings)
    .jvmSettings(
    )
    .jsSettings(
    )

lazy val endermanJVM = enderman.jvm
lazy val endermanJS = enderman.js

