import Dependencies._

ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"

lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }

scalacOptions += "-deprecation"

lazy val scalafxDependencies = Seq(
  "org.scalafx" % "scalafx_3" % "20.0.0-R31",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
) ++ Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
  .map(m => "org.openjfx" % s"javafx-$m" % "21.0.6" classifier osName)

lazy val commonSettings = Seq(
    scalaVersion := "3.3.5",
    Test / parallelExecution := false,
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    libraryDependencies ++= scalafxDependencies,
    
    //not nessescary
    javaOptions ++= Seq(
      "--module-path", (Compile / fullClasspath).value
        .map(_.data.getAbsolutePath)
        .filter { path =>
          (osName == "win" && path.endsWith("-win.jar")) ||
          (osName == "linux" && path.endsWith("-linux.jar")) ||
          (osName == "mac" && path.endsWith("-mac.jar"))
        }
        .mkString(System.getProperty("path.separator")),
      "--add-modules", "javafx.controls,javafx.fxml"
    ),
    fork := true
  )

