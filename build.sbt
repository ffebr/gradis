val scala3Version = "3.8.3"

fork := true
scalacOptions ++= Seq("-encoding", "utf8")

javaOptions ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8"
)

Global / excludeLintKeys ++= Set(nativeImageJvm, nativeImageVersion)

lazy val root = project
  .in(file("."))
  .enablePlugins(BuildInfoPlugin, NativeImagePlugin)
  .settings(
    name := "gradis",
    buildInfoPackage := "cli",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion),
    scalaVersion := scala3Version,

    Compile / mainClass := Some("Gradis"),
    nativeImageJvm := "graalvm-java17",
    nativeImageVersion := "22.3.1",
    nativeImageOutput := file("target/native-image/gradis"),
    nativeImageOptions ++= Seq(
      "-H:+ReportExceptionStackTraces",
      "--no-fallback",
      "-O2"
    ),

    libraryDependencies += "org.scalameta" %% "munit"   % "1.3.0" % Test,
    libraryDependencies += "dev.zio"       %% "zio"     % "2.1.25",
    libraryDependencies += "dev.zio"       %% "zio-cli" % "0.7.4"
  )
