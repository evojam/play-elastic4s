publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
    <scm>
      <url>git@github.com:evojam/play-elastic4s.git</url>
      <connection>scm:git:git@github.com:evojam/play-elastic4s.git</connection>
    </scm>
    <developers>
      <developer>
        <id>WojciechP</id>
        <name>Wojciech Ptak</name>
      </developer>
      <developer>
        <id>duketon</id>
        <name>Michael Kendra</name>
      </developer>
      <developer>
        <id>abankowski</id>
        <name>Artur Bankowski</name>
      </developer>
    </developers>)

licenses := Seq("Apache 2.0 License" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/evojam/play-elastic4s"))
