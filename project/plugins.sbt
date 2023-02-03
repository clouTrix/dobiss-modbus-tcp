addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9" )
addSbtPlugin("com.github.sbt" % "sbt-release"         % "1.1.0" )
addSbtPlugin("org.scoverage"  % "sbt-scoverage"       % "2.0.6" )
addSbtPlugin("com.eed3si9n"   % "sbt-assembly"        % "2.1.0" )

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
