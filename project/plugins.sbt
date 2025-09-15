addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.3" )
addSbtPlugin("com.github.sbt" % "sbt-release"         % "1.4.0" )
addSbtPlugin("org.scoverage"  % "sbt-scoverage"       % "2.3.1" )
addSbtPlugin("com.eed3si9n"   % "sbt-assembly"        % "2.3.1" )

ThisBuild / libraryDependencySchemes ++= Seq(
    "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
