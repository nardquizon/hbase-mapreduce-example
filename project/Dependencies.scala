import sbt.{ExclusionRule, _}

object Dependencies {
  val hadoopVersion = "2.6.0"
  val hbaseVersion = "1.1.2"
  val slf4jVersion = "1.7.25"

  val commonsBeanUtils = "commons-beanutils" % "commons-beanutils" % "1.9.2"

  val hadoopClient = "org.apache.hadoop" % "hadoop-client" % hadoopVersion
  val hadoopCommon = "org.apache.hadoop" % "hadoop-common" % hadoopVersion

  val hadoopHdfs = "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion
  val hadoopMapreduceClientJobClient = "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % hadoopVersion

  val hbaseClient = "org.apache.hbase" % "hbase-client" % hbaseVersion
  val hbaseCommon = "org.apache.hbase" % "hbase-common" % hbaseVersion

  val hbaseServer = "org.apache.hbase" % "hbase-server" % hbaseVersion

  val hbaseHadoopCompat = "org.apache.hbase" % "hbase-hadoop-compat" % hbaseVersion excludeAll(
    exclusions.commonsLogging
    )

  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % slf4jVersion
  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion

  val scalaArm = "com.jsuereth" %% "scala-arm" % "2.0"

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

  object exclusions {
    val commonsLogging = ExclusionRule("commons-logging", "commons-logging")
  }
}