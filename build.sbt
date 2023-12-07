ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "HelloWorld"
  )
libraryDependencies ++= Seq(
  "software.amazon.awssdk" % "s3" % "2.17.42",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.mockito" %% "mockito-scala" % "1.16.42" % Test
)
