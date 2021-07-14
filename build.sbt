name := "gatling-mqtt"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-core" % "3.6.1" % "provided",
  "org.fusesource.mqtt-client" % "mqtt-client" % "1.16"
)

// Gatling contains scala-library
assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)
