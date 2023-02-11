import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

ThisBuild / name             := "dobiss-modbus-tcp"
ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / organization     := "com.cloutrix"
ThisBuild / organizationName := "clouTrix"
ThisBuild / mainClass        := Some("cloutrix.energy.DobissModbusTcpProxy")

lazy val JavaOpts = Seq(
  "-Xmx24m",
  "-Xms12m"
)

lazy val dockerSettings = Seq(
  Docker/ packageName     := "cloutrix/dobiss-modbus-tcp",
  Docker / daemonUser     := "dobiss",
  Docker / daemonGroup    := "dobiss",
  Docker / daemonUserUid  := Some("1666"),
  Docker / daemonGroupGid := Some("1666"),
  dockerBaseImage := "alpine:3.17",
  dockerBasePackages := Seq(),
  dockerBaseInstaller := Some(packages => s"apk update && apk upgrade && apk add --update ${packages.mkString(" ")}"),
  dockerJdkImage := Some("azul/zulu-openjdk-alpine:17"),
  bundledJvmLocation := Some("jre"),
  dockerJreModuleReplace := true, // we define the java modules ourselves to keep the container as small as possible
  dockerJreModules := Seq("java.base", "java.sql", "java.naming"),

  dockerExposedPorts      := Seq(1502),
  Universal / javaOptions := JavaOpts.map("-J" + _)
)

lazy val releaseBuildSettings = Seq(
  releaseVersionBump := sbtrelease.Version.Bump.Bugfix,
  releaseVersionFile := baseDirectory.value / "version.sbt",

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
