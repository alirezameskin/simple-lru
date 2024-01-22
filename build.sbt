ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "simplelru",
    libraryDependencies ++= Seq(
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test
    )
  )

addCommandAlias("fmt", "; Compile / scalafmt; Test / scalafmt; scalafmtSbt")
