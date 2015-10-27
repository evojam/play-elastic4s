scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.11.1", "2.11.4", "2.11.7")

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sbtPluginRepo("snapshots"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.typesafeRepo("snapshots"),
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeIvyRepo("releases")
)

val PlayVersion = "2.4.3"

libraryDependencies ++= Seq(
  "com.typesafe.play" % "play_2.11" % PlayVersion,
  "com.typesafe.play" % "play-json_2.11" % PlayVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-core" % "1.7.4"
  //"com.sksamuel.elastic4s" %% "elastic4s-streams" % "1.7.4"
)