import com.typesafe.sbt.packager.docker.ExecCmd

name := "ScalaProject"

version := "0.1"

scalaVersion := "2.13.4"

val AkkaVersion = "2.6.10"
val circeVersion = "0.12.3"
val AkkaHttpVersion = "10.2.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.31.0",
)

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion

libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.30" % Test
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

enablePlugins(JavaAppPackaging, AshScriptPlugin)

dockerBaseImage := "openjdk:8-jre-alpine"
packageName in Docker := "chat-app-akka"

dockerCommands := dockerCommands.value.map {
  case ExecCmd("CMD", _ @ _*) =>
    ExecCmd("CMD", "/opt/docker/bin/ChatApplicationBoot")
  case other =>
    other
}
