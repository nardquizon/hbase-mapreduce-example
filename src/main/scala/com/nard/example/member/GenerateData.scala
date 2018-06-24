package com.nard.example.member

import java.security.MessageDigest

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor}
import org.joda.time.DateTime
import resource.managed

import scala.util.Random

/**
  * Generate sample data
  *
  * Creates an hbase table "member" with column family "attribute"
  * Then generates data for:
  * 1. attribute:name
  * 2. attribute:age
  * 3. attribute:bdate
  *
  */
object GenerateData {

  def main(args: Array[String]): Unit = {
    implicit val conf = HBaseConfiguration.create()

    createTable()
    generateData()
  }

  def createTable()(implicit conf: Configuration): Unit = {
    for {
      conn <- managed(ConnectionFactory.createConnection(conf))
    } {
      val adminConn = conn.getAdmin

      if (!adminConn.tableExists(MemberTable)) {
        val table = new HTableDescriptor(MemberTable)
        val cf = new HColumnDescriptor(AttributeFamily)
        table.addFamily(cf)
        adminConn.createTable(table)
      }
    }
  }

  def generateData()(implicit conf: Configuration) = {
    val now = DateTime.now()
    for {
      conn <- managed(ConnectionFactory.createConnection(conf))
    } {
      for {
        i <- 1 to 1000
      } {
        val rowKey = toRowKey("M-" + "%09d".format(i))
        val put = new Put(rowKey)
        val name =
          Random.alphanumeric.take(100).filter(_.isLetter).take(10).mkString
        val sex = if (Random.nextInt() % 3 == 0) "F" else "M"
        val bdate =
          if (sex.equals("M"))
            now.minusDays(Math.abs(Random.nextInt() % 43800)).getMillis
          else
            now.minusDays(Math.abs(Random.nextInt() % 28200)).getMillis
        // Add bdate to attribute column family
        put.addColumn(AttributeFamily, BirthdateQualifier, Bytes.toBytes(bdate))
        // Add name to attribute column family
        put.addColumn(AttributeFamily, NameQualifier, Bytes.toBytes(name))
        // Add sex to attribute column family
        put.addColumn(AttributeFamily, SexQualifier, Bytes.toBytes(sex))
        conn.getTable(MemberTable).put(put)
      }
    }
  }

  def toRowKey(key: String): Array[Byte] = {
    val bytes = MessageDigest.getInstance("MD5").digest(Bytes.toBytes(key))
    val lastByte = bytes(bytes.length - 1)
    Bytes.add(Array(lastByte), Bytes.toBytes(key))
  }
}
