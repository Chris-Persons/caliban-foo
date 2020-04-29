name := "caliban-foo"

version := "0.1"

scalaVersion := "2.12.11"

libraryDependencies += "com.github.ghostdogpr" %% "caliban" % "0.7.6"
libraryDependencies += "com.github.ghostdogpr" %% "caliban-akka-http" % "0.7.6" // routes for akka-http
libraryDependencies +=  "de.heikoseeberger" %% "akka-http-play-json" % "1.32.0" % Optional

