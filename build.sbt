organization := "org.chepurnoy"

name := "crawler"

version := "0.2.3"

scalaVersion := "2.10.1"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("typesafe" at "http://repo.typesafe.com/typesafe/repo", 
		          "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                  "releases"  at "http://oss.sonatype.org/content/repositories/releases")

libraryDependencies ++=  Seq(
        "org.jsoup" % "jsoup" % "1.7.2",
        "com.github.theon" % "scala-uri_2.10" % "0.3.6",
        "joda-time" % "joda-time" % "2.1",
        "org.joda" % "joda-convert" % "1.2",
        "com.typesafe.play" % "play-iteratees_2.10" % "2.2.1",
        "com.typesafe.akka" % "akka-actor_2.10" % "2.2.1",
        "com.typesafe.akka" % "akka-agent_2.10" % "2.2.1" exclude("org.scala-stm", "scala-stm_2.10.0"),
        "com.typesafe.akka" % "akka-slf4j_2.10" % "2.2.1")

val logback = "ch.qos.logback" % "logback-classic" % "1.0.13"

publishMavenStyle := true

publishTo := Some(Resolver.file("crawler",  new File( "./repo" )))
