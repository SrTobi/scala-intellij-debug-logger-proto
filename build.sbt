import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

lazy val commonSettings = Seq(
  organization := "com.github.srtobi",
  scalaVersion := "2.13.2",

  idePackagePrefix := Some("com.github.srtobi"),

  scalacOptions ++= Seq("-feature", "-Xfatal-warnings", "-deprecation", "-unchecked", "-Ymacro-annotations"),

  //libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0" % Test,
  //libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % Test,
)

lazy val root = project
  .in(file("."))
  .aggregate(native, web, coreJS, coreJVM)
  .settings(commonSettings)
  .settings(
    name := "debug-logger"
  )

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "debug-logger-core",
    publish := {},
    publishLocal := {},

    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "sourcecode" % "latest.release",
      "com.lihaoyi" %%% "upickle" % "latest.release"
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val web = project
  .in(file("web"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(coreJS)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := false,
    libraryDependencies += "org.lrng.binding" %%% "html" % "latest.release",
    copyTask("web/html")
  )

lazy val native = project
  .in(file("playground"))
  .dependsOn(coreJVM)
  .settings(commonSettings)

def copyTask(odir: String) = {
  lazy val copyJSOutput = taskKey[Unit]("copy scala.js linker outputs to another location")
  Seq(
    copyJSOutput := {
      println(s"Copying artifact ${scalaJSLinkedFile.in(Compile).value.path} to [${odir}]")
      val src = file(scalaJSLinkedFile.in(Compile).value.path)
      IO.copy(Seq(
        (src, file(odir) / src.name),
        (file(src.getCanonicalPath + ".map"), file(odir) / (src.name + ".map"))
      ), CopyOptions(overwrite = true, preserveLastModified = true, preserveExecutable = true))
    },
    fastOptJS / copyJSOutput := (copyJSOutput triggeredBy fastOptJS.in(Compile)).value,
    fullOptJS / copyJSOutput := (copyJSOutput triggeredBy fullOptJS.in(Compile)).value
  )
}