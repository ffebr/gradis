val scala3Version = "3.8.3"

fork := true
javaOptions += "-Dfile.encoding=UTF-8"
scalacOptions ++= Seq("-encoding", "utf8")

javaOptions ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8"
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "gradis",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit"   % "1.3.0" % Test,
    libraryDependencies += "dev.zio"       %% "zio"     % "2.1.25",
    libraryDependencies += "dev.zio"       %% "zio-cli" % "0.7.4"
  )
