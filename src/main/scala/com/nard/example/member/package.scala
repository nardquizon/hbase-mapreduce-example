package com.nard.example

import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.util.Bytes

/**
  * Package Object
  */
package object member {
  val MemberTable = TableName.valueOf("member")
  val AttributeFamily = Bytes.toBytes("attribute")
  val BirthdateQualifier = Bytes.toBytes("bdate")
  val NameQualifier = Bytes.toBytes("name")
  val SexQualifier = Bytes.toBytes("sex")
}
