import sbtcrossproject.{crossProject, CrossType}

lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"

lazy val jsResources = taskKey[Seq[File]](
  "All scalajs generated JS files, including source maps"
)

lazy val prodJsResources = taskKey[Seq[File]](
  "All scalajs generated JS files, including source maps"
)

jsResources := {
  // this sets up a dependency on fastOptJS. For production, we'd want to run
  // fullOptJs instead
  val fastOpt = (fastOptJS in (client, Compile)).value.data
  val dir = (crossTarget in (client, Compile)).value
  dir.listFiles.filter(f => f.getName.endsWith(".js") || f.getName.endsWith(".js.map"))
}

prodJsResources := {
  val fullOpt = (fullOptJS in (client, Compile)).value.data
  val dir = (crossTarget in (client, Compile)).value
  dir.listFiles.filter(f => f.getName.endsWith(".js") || f.getName.endsWith(".js.map"))
}

lazy val commonSettings = Seq(
  organization := "org.langchao",
  scalaVersion := "2.12.5",
  version := "0.3.5"
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
    "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
  )
)

lazy val testServer = (project in file("build/test"))
  .settings(
    commonSettings,
    mainClass in Compile := Some("enderman.Main"),
    (resources in Compile) := {
      (resources in Compile).value ++ (jsResources in LocalRootProject).value
    },
    dockerBaseImage := "openjdk:jre-alpine",
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
    (resources in Compile) := {
      (resources in Compile).value ++ (prodJsResources in LocalRootProject).value
    },
    dockerBaseImage := "openjdk:jre-alpine",
    dockerUpdateLatest := true,
    packageName in Docker := "enderman"
  ).enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .dependsOn(server)

lazy val client = (project in file("client")).settings(
  commonSettings,
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "com.thoughtworks.binding" %%% "dom" % "latest.release"
  )
).enablePlugins(ScalaJSPlugin)


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

