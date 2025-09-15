import sbt.*

object Version {
    final val Jsoniter = "2.31.1"
    final val Logback = "1.2.13"
    final val Logging = "3.9.5"
    final val Config = "1.4.3"
    final val ScalaSttp = "4.0.11"
    final val Netty = "4.2.6.Final"
    final val Xml = "2.3.0"
    final val NimbusJoseJwt = "10.5"

    final val ScalaTest = "3.2.19"
}

object Deps {
    final val Jsoniter: ModuleID = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % Version.Jsoniter excludeAll ExclusionRule("org.scala-lang")
    final val JsoniterMacros: ModuleID = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % Version.Jsoniter % "provided"
    final val NimbusJoseJwt: ModuleID = "com.nimbusds" % "nimbus-jose-jwt" % Version.NimbusJoseJwt

    final val Logback: ModuleID = "ch.qos.logback" % "logback-classic" % Version.Logback //exclude("org.slf4", "slf4j-api") excludeAll (ExclusionRule("javax.mail"), ExclusionRule("javax.servlet"), ExclusionRule("org.codehaus.janino"))
    final val Logging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % Version.Logging excludeAll ExclusionRule("org.scala-lang")

    final val Config: ModuleID = "com.typesafe" % "config" % Version.Config
    final val ScalaSttp: ModuleID = "com.softwaremill.sttp.client4" %% "core" % Version.ScalaSttp excludeAll ExclusionRule("org.scala-lang")
    final val ScalaSttpOkHttp: ModuleID = "com.softwaremill.sttp.client4" %% "okhttp-backend" % Version.ScalaSttp excludeAll ExclusionRule("org.scala-lang")
    final val ScalaSttpJsoniter: ModuleID = "com.softwaremill.sttp.client4" %% "jsoniter" % Version.ScalaSttp excludeAll ExclusionRule("org.scala-lang")
    final val Xml: ModuleID = "org.scala-lang.modules" %% "scala-xml" % Version.Xml

    final val NettyCodec: ModuleID = "io.netty" % "netty-codec" % Version.Netty
    final val NettyHandler: ModuleID = "io.netty" % "netty-handler" % Version.Netty

    object All {
        final val Http: Seq[ModuleID] = Seq(Deps.ScalaSttp, Deps.ScalaSttpOkHttp, Deps.ScalaSttpJsoniter, Deps.NimbusJoseJwt)
        final val Netty: Seq[ModuleID] = Seq(Deps.NettyCodec, Deps.NettyHandler)
        final val Jsoniter: Seq[ModuleID] = Seq(Deps.Jsoniter, Deps.JsoniterMacros)
        final val Logging: Seq[ModuleID] = Seq(Deps.Logging, Deps.Logback)
    }
}

object TestDeps {
    final val ScalaTest: ModuleID = "org.scalatest" %% "scalatest" % Version.ScalaTest % Test
}
