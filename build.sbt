name := "akka-csv-stream"

version := "1.0.0-SNAPSHOT"

organization := "com.bijansoft"

scalaVersion := "2.12.3"

lazy val akkaVersion = "2.5.4"
lazy val akkaHttpVersion = "10.0.9"
lazy val akkaAlpakka = "0.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % akkaAlpakka,
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % akkaAlpakka
)

//scalacOptions ++= Seq(
//  //  "-Xprint:parser",
//  "-deprecation",
//  "-encoding",
//  "UTF-8",
//  "-feature",
//  "-unchecked",
//  "-language:higherKinds",
//  "-language:implicitConversions",
//  "-Xlint",
//  "-Yno-adapted-args",
//  "-Ywarn-dead-code",
//  "-Ywarn-numeric-widen",
//  "-Ywarn-value-discard",
//  "-Xfuture",
//  "-opt:l:classpath",
//  "-Ywarn-unused-import"
//)
//
//fork in run := true
//
//javaOptions in run ++= Seq(
//  "-Xms32m",
//  "-Xmx32m",
//  "-XX:+UseG1GC",
//  //"-XX:+UseParallelGC",
//  "-XX:MaxGCPauseMillis=400"
//)
