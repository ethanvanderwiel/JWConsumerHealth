scalaVersion in ThisBuild := "2.11.8"

val bannoScalaLibrariesVersion = "7.0.0"
val bannoVaultScalaApiVersion = "3.3.0"
val doobieVersion = "0.4.1"
val http4sVersion = "0.15.12a"
val log4sVersion = "1.3.4"
val pureConfigVersion = "0.7.0"
val scalazVersion = "7.2.12"
val scalazStreamVersion = "0.8.6a"
val scalaTestVersion = "3.0.1"
val shapelessVersion = "2.3.2"
val specs2Version = "3.8.9"
val argonautVersion = "6.2"
val enumeratumVersion = "1.5.12"

lazy val `template-service` =
  project.in(file("."))

name := "template-service"

enablePlugins(BannoDockerPlugin)
bannoDockerDefaultHeapSize := "128m"

bannoEnableCompileLinter := false
wartremoverErrors in (Compile, compile) ++= Warts.unsafe

libraryDependencies ++= Seq(
  "com.banno" %% "logging-shim" % bannoScalaLibrariesVersion,
  "com.banno" %% "banno-config" % bannoScalaLibrariesVersion,
  "com.banno" %% "banno-health" % bannoScalaLibrariesVersion,
  "com.banno" %% "scala-vault-api" % bannoVaultScalaApiVersion,
  "com.beachape" %% "enumeratum-argonaut" % enumeratumVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M5",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.7" % Test,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "io.argonaut" %% "argonaut-scalaz" % argonautVersion,
  "org.http4s" %% "http4s-argonaut" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-server-metrics" % http4sVersion,
  "org.log4s" %% "log4s" % log4sVersion,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-scalaz" % specs2Version % Test,
  "org.typelevel" %% "scalaz-scalatest" % "1.1.2" % Test
)

bannoReleaseGitPushOnlyTag := true
publishArtifact in ThisBuild := false
