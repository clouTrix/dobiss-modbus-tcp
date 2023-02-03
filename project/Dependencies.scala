import sbt._

object Version {
  final val Jsoniter = "2.18.0"
  final val Logback = "1.2.11"
  final val Logging = "3.9.5"
  final val Config = "1.4.2"
  final val ScalaHttp = "2.4.2"
  final val Netty = "4.1.87.Final"

  final val ScalaTest = "3.2.10"
}

object Deps {
  final val Jsoniter: ModuleID = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % Version.Jsoniter excludeAll ExclusionRule("org.scala-lang")
  final val JsoniterMacros: ModuleID = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Version.Jsoniter % "provided"

  final val Logback: ModuleID = "ch.qos.logback" % "logback-classic" % Version.Logback exclude("org.slf4", "slf4j-api") excludeAll (ExclusionRule("javax.mail"), ExclusionRule("javax.servlet"), ExclusionRule("org.codehaus.janino"))
  final val Logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Version.Logging excludeAll ExclusionRule("org.scala-lang")

  final val Config: ModuleID = "com.typesafe" % "config" % Version.Config
  final val ScalaHttp: ModuleID = "org.scalaj" %% "scalaj-http" % Version.ScalaHttp excludeAll ExclusionRule("org.scala-lang")

  final val NettyCodec: ModuleID = "io.netty" % "netty-codec" % Version.Netty
  final val NettyHandler: ModuleID = "io.netty" % "netty-handler" % Version.Netty

  object All {
    final val Netty: Seq[ModuleID] = Seq(Deps.NettyCodec, Deps.NettyHandler)
    final val Jsoniter: Seq[ModuleID] = Seq(Deps.Jsoniter, Deps.JsoniterMacros)
    final val Logging: Seq[ModuleID] = Seq(Deps.Logging, Deps.Logback)
  }
}

object TestDeps {
  final val ScalaTest: ModuleID = "org.scalatest" %% "scalatest" % Version.ScalaTest % Test
}
