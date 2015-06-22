name := "optimization-graph-coloring"
 
version := "1.0"
 
scalaVersion := "2.11.6"

scalaSource in Compile := baseDirectory.value
 
scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings",
  "-encoding", "UTF-8"
)
