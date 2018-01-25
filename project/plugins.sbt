resolvers += Resolver.sonatypeRepo("public")

resolvers ++= Seq(
  "Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots",
  "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases",
  "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/",
  "Oncue Bintray Repo" at "https://dl.bintray.com/oncue/releases"
)

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.15")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.3")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.0")
addSbtPlugin("com.banno" % "banno-sbt-plugin" % "10.0.1")