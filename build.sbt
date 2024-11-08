import sbt.*
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations.*

ThisBuild / name             := "dobiss-modbus-tcp"
ThisBuild / scalaVersion     := "2.13.15"
ThisBuild / organization     := "com.cloutrix"
ThisBuild / organizationName := "clouTrix"
ThisBuild / mainClass        := Some("cloutrix.energy.DobissModbusTcpProxy")

lazy val JavaOpts = Seq(
  "-Xmx64m",
  "-Xms20m"
)

lazy val dockerSettings = Seq(
  Docker/ packageName     := "cloutrix/dobiss-modbus-tcp",
  Docker / daemonUser     := "dobiss",
  Docker / daemonGroup    := "dobiss",
  Docker / daemonUserUid  := Some("1666"),
  Docker / daemonGroupGid := Some("1666"),
  dockerBaseImage         := "azul/zulu-openjdk-alpine:17-jre-headless",
  dockerExposedPorts      := Seq(1502),
  Universal / javaOptions := JavaOpts.map("-J" + _)
)

lazy val releaseBuildSettings = Seq(
  releaseVersionBump := sbtrelease.Version.Bump.Bugfix,
  releaseVersionFile := baseDirectory.value / "version.sbt",

  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  releaseIgnoreUntrackedFiles := true,

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepTask(Docker / publish),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val coreDependencies = Seq(
  libraryDependencies ++= Deps.All.Logging,
  libraryDependencies ++= Deps.All.Jsoniter,
  libraryDependencies ++= Deps.All.Netty,
  libraryDependencies ++= Seq(
    Deps.ScalaHttp,
    Deps.Config,
    Deps.Xml
  )
)

lazy val testSettings = Seq(
  Test / fork               := true,
  Test / parallelExecution  := false,
  libraryDependencies       += TestDeps.ScalaTest,
  Test / javaOptions        := JavaOpts
)

lazy val assemblySettings = Seq(
  assembly / assemblyMergeStrategy := {
    //workaround for conflict with netty manifest files
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
    case x                                                    => (assembly / assemblyMergeStrategy).value(x)
  }
)

lazy val DobissModbusProxy = (project in file("."))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging, AshScriptPlugin)
  .settings(
    coreDependencies,
    assemblySettings,
    dockerSettings,
    releaseBuildSettings,
    testSettings
  )
