//resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin(("org.scommons.sbt" % "sbt-scommons-plugin" % "0.5.0-SNAPSHOT").changing())
//addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin" % "0.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.3")
