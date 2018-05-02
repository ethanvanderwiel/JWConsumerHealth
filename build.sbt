// Scala
val catsV = "1.1.0"
val catsEffectV = "0.10"
val fs2V = "0.10.3"
val http4sV = "0.18.9"
val circeV = "0.9.2"
val doobieV = "0.5.1"
val pureConfigV = "0.9.1"
val specs2V = "4.0.3"
// Java
val flyWayV = "5.0.7"
val logbackClassicV = "1.2.3"
val logstashEncoderV = "4.11"
val dropwizardMetricsV = "4.0.2"
// Banno
val vault4sV = "3.1.0"
val zookeeperV = "1.23.0"

lazy val `template-service` =
  project.in(file("."))
    .enablePlugins(BannoDockerPlugin)
    .settings(commonSettings)
    .settings(
      name := "template-service"
    )

lazy val commonSettings = Seq(
  organization := "com.banno",
  scalaVersion := "2.12.4",

  bannoDockerDefaultHeapSize := "128m",
  scalacOptions ++= Seq("-Xmax-classfile-name","242"),
  publishArtifact in ThisBuild := false,
  bannoReleaseGitPushOnlyTag := true,

  cancelable in Scope.Global := true,

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.6" cross CrossVersion.binary),

  libraryDependencies ++= Seq(
    "com.banno"                   %% "vault4s"                      % vault4sV,
    "com.banno"                   %% "zookeeper-http4s"             % zookeeperV,
    "com.banno"                   %% "simple-health-dropwizard"     % "0.6.0",
    "com.banno"                   %% "simple-health-prometheus"     % "0.6.0",


    "org.typelevel"               %% "cats-core"                    % catsV,
    "org.typelevel"               %% "cats-effect"                  % catsEffectV,
    "co.fs2"                      %% "fs2-io"                       % fs2V,
    "org.http4s"                  %% "http4s-dsl"                   % http4sV,
    "org.http4s"                  %% "http4s-blaze-server"          % http4sV,
    "org.http4s"                  %% "http4s-blaze-client"          % http4sV,
    "org.http4s"                  %% "http4s-circe"                 % http4sV,
    "org.http4s"                  %% "http4s-server-metrics"        % http4sV,
    "io.circe"                    %% "circe-generic"                % circeV,
    "io.circe"                    %% "circe-parser"                 % circeV,
    "org.tpolecat"                %% "doobie-core"                  % doobieV,
    "org.tpolecat"                %% "doobie-hikari"                % doobieV,
    "org.tpolecat"                %% "doobie-postgres"              % doobieV,
    "com.github.pureconfig"       %% "pureconfig"                   % pureConfigV,

    "org.flywaydb"                % "flyway-core"                   % flyWayV,
    "ch.qos.logback"              % "logback-classic"               % logbackClassicV,
    "net.logstash.logback"        % "logstash-logback-encoder"      % logstashEncoderV,
    "io.dropwizard.metrics"       % "metrics-core"                  % dropwizardMetricsV,
    "io.dropwizard.metrics"       % "metrics-jvm"                   % dropwizardMetricsV,
    "io.dropwizard.metrics"       % "metrics-graphite"              % dropwizardMetricsV,
    "io.dropwizard.metrics"       % "metrics-healthchecks"          % dropwizardMetricsV,
    "io.dropwizard.metrics"       % "metrics-json"                  % dropwizardMetricsV,

    "org.tpolecat"                %% "doobie-specs2"                % doobieV   % Test,

    "org.specs2"                  %% "specs2-core"                  % specs2V      % Test,
    "org.specs2"                  %% "specs2-scalacheck"            % specs2V       % Test,
    "org.typelevel"               %% "discipline"                   % "0.8"         % Test,
    "com.github.alexarchambault"  %% "scalacheck-shapeless_1.13"    % "1.1.6"       % Test
  )
)
