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

lazy val `template-service` =
  project.in(file("."))

name := "template-service"

enablePlugins(BannoDockerPlugin)
bannoDockerDefaultHeapSize := "128m"

bannoEnableCompileLinter := false
wartremoverErrors in (Compile, compile) ++= Warts.unsafe

libraryDependencies ++= Seq(
  "com.banno"    %% "logging-shim"          % bannoScalaLibrariesVersion,
  "com.banno"    %% "banno-config"          % bannoScalaLibrariesVersion,
  "com.banno"    %% "banno-health"          % bannoScalaLibrariesVersion,
  "com.banno"    %% "scala-vault-api"       % bannoVaultScalaApiVersion,
  "com.chuusai"  %% "shapeless"             % shapelessVersion,
  "com.github.pureconfig" %% "pureconfig"   % pureConfigVersion,
  "org.http4s"   %% "http4s-argonaut"       % http4sVersion,
  "org.http4s"   %% "http4s-blaze-server"   % http4sVersion,
  "org.http4s"   %% "http4s-dsl"            % http4sVersion,
  "org.http4s"   %% "http4s-server-metrics" % http4sVersion,
  "org.log4s"    %% "log4s"                 % log4sVersion,
  "org.tpolecat" %% "doobie-core"           % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"         % doobieVersion % Test,
  "org.tpolecat" %% "doobie-hikari"         % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"       % doobieVersion,
  "org.tpolecat" %% "doobie-h2"             % doobieVersion % Test,
  "org.scalaz"   %% "scalaz-concurrent"     % scalazVersion,
  "org.scalaz"   %% "scalaz-core"           % scalazVersion,
  "org.specs2"   %% "specs2-core"           % specs2Version % Test,
  "org.specs2"   %% "specs2-scalaz"         % specs2Version % Test
)

bannoReleaseGitPushOnlyTag := true
bannoReleasePostCommitBeforeTagStep := updateMarathonConfigsAndCommit
publishArtifact in ThisBuild := false

lazy val updateMarathonConfigsAndCommit = ReleaseStep(
  action = (st: State) => {
    val extracted = Project.extract(st)
    val v = extracted.get(version)
    val exitCode =
      (Process("./update-json-version.sh" :: v :: Nil, extracted.get(baseDirectory) / "deployment/marathon") #&&
         Process("git" :: "commit" :: "-a" :: "-m" :: s"Updating marathon configs to version $v" :: Nil) !)
    if (exitCode != 0) sys.error("Did not update marathon version! Command failed.")
    st
  }
)
