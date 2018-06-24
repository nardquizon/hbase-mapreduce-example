import Dependencies._

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.nardquizon",
      scalaVersion := "2.12.4"
    )),
  name := "hbase-mapreduce-examples",
  assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
  libraryDependencies ++= Seq(
    hbaseServer,
    hadoopCommon,
    hadoopHdfs,
    hadoopMapreduceClientJobClient,
    hbaseHadoopCompat,
    hbaseCommon,
    hbaseClient,
    joda,
    scalaArm
  )
)
