name := "crawler"

version := "0.1.0"

scalaVersion := "2.10.2"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/repo"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
                    "releases"  at "http://oss.sonatype.org/content/repositories/releases")

libraryDependencies ++=  Seq(
        "joda-time" % "joda-time" % "2.1",
        "org.joda" % "joda-convert" % "1.2",
        "org.specs2" %% "specs2" % "2.2.3" % "test",
         "com.typesafe.akka" %% "akka-actor" % "2.2.1",
            "com.typesafe.akka" %% "akka-agent" % "2.2.1" exclude("org.scala-stm", "scala-stm_2.10.0"),
            "com.typesafe.akka" %% "akka-slf4j" % "2.2.1")

publishMavenStyle := true

publishTo := Some(Resolver.file("timeseries",  new File( "/home/serjk/workspaces/work/SimpleCrawler/repository" )) )
