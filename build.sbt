name := """SonosCLI"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies +=  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "org.scala-sbt" % "command" % "0.12.0"

mainClass in assembly := Some("com.jmengers.sonoscli.Main")
