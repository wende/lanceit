name := "play2-template"

version := "1.0"

lazy val `play2-template` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies +=  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"

libraryDependencies ++=  Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23")


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  