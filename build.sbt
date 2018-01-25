// Scala
val catsV = "1.0.1"
val kittensV = "1.0.0-RC2"
val catsEffectV = "0.8"
val fs2V = "0.10.0-M11"
val http4sV = "0.18.0-M8"
val circeV = "0.9.0"
val doobieV = "0.5.0-M12"
val pureConfigV = "0.9.0"
// Java
val flyWayV = "5.0.5"
val logbackClassicV = "1.2.3"
val logstashEncoderV = "4.11"
val dropwizardMetricsV = "3.1.4"
val prometheusClientV = "0.0.26"
// Banno
val vault4sV = "1.4.0"
val zookeeperV = "1.11.0"

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

  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.4" cross CrossVersion.binary),

  libraryDependencies ++= Seq(
    "com.banno"                   %% "vault4s"                      % vault4sV,
    "com.banno"                   %% "zookeeper"                    % zookeeperV,

    "org.typelevel"               %% "cats-core"                    % catsV,
    "org.typelevel"               %% "kittens"                      % kittensV,
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
    "io.prometheus"               % "simpleclient_common"           % prometheusClientV,
    "io.prometheus"               % "simpleclient_hotspot"          % prometheusClientV,
    "io.prometheus"               % "simpleclient_dropwizard"       % prometheusClientV,
    "io.prometheus"               % "simpleclient_graphite_bridge"  % prometheusClientV,

    "org.tpolecat"                %% "doobie-specs2"                % doobieV   % Test,


    "org.specs2"                  %% "specs2-core"                  % "4.0.1"       % Test,
    "org.specs2"                  %% "specs2-scalacheck"            % "4.0.1"       % Test,
    "org.typelevel"               %% "discipline"                   % "0.8"         % Test,
    "com.github.alexarchambault"  %% "scalacheck-shapeless_1.13"    % "1.1.6"       % Test
  )
)
