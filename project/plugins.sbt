addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.4" )
addSbtPlugin("com.github.sbt" % "sbt-release"         % "1.4.0" )
addSbtPlugin("org.scoverage"  % "sbt-scoverage"       % "2.2.2" )
addSbtPlugin("com.eed3si9n"   % "sbt-assembly"        % "2.3.0" )

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
