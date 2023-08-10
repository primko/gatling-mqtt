name := "gatling-mqtt"

version := "1.0.0"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "io.gatling"                 % "gatling-core" % "3.9.2" % "provided",
  "org.fusesource.mqtt-client" % "mqtt-client"  % "1.16"
)

// Gatling contains scala-library
assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)
