name := """lode-runner-bot-scala"""

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test")

libraryDependencies += "io.backchat.hookup" % "hookup_2.10" % "0.4.0"

libraryDependencies += "org.scala-lang" % "scala-swing" % scalaVersion.value


