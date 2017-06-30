resolvers += Resolver.sonatypeRepo("public")

resolvers ++= Seq("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots",
  "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases",
  "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/",
  "Oncue Bintray Repo" at "https://dl.bintray.com/oncue/releases") // TODO: Should this be necessary?

addSbtPlugin("com.banno" % "banno-sbt-plugin" % "9.2.0")

scalacOptions += "-deprecation"

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.0.3")
