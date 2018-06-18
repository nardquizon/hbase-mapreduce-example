package com.nard.example.member

import java.io.IOException
import java.lang

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Mutation, Put, Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{
  TableMapReduceUtil,
  TableMapper,
  TableReducer
}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}

/**
  * MapReduce example for modifying the column "attribute:sex" values
  * 1. "M" to "Male"
  * 2. "F" to "Female"
  */
object MapReduceModify {
  class TheMapper
      extends TableMapper[ImmutableBytesWritable, ImmutableBytesWritable] {
    type Context =
      Mapper[ImmutableBytesWritable,
             Result,
             ImmutableBytesWritable,
             ImmutableBytesWritable]#Context

    override def map(key: ImmutableBytesWritable,
                     value: Result,
                     context: Context): Unit = {

      val sex = Bytes.toString(value.getValue(AttributeFamily, SexQualifier))

      val modified =
        if (sex.equals("M")) {
          new ImmutableBytesWritable(Bytes.toBytes("Male"))
        } else {
          new ImmutableBytesWritable(Bytes.toBytes("Female"))
        }

      context.write(key, modified)
    }
  }

  class TheReducer
      extends TableReducer[ImmutableBytesWritable,
                           ImmutableBytesWritable,
                           ImmutableBytesWritable] {

    type Context = Reducer[ImmutableBytesWritable,
                           ImmutableBytesWritable,
                           ImmutableBytesWritable,
                           Mutation]#Context

    override def reduce(key: ImmutableBytesWritable,
                        values: lang.Iterable[ImmutableBytesWritable],
                        context: Context): Unit = {
      var sex: Array[Byte] = Bytes.toBytes("")
      val it = values.iterator()
      if (it.hasNext) {
        sex = it.next().get()
      }
      val put = new Put(key.get())
      put.addColumn(AttributeFamily, SexQualifier, sex)

      context.write(key, put)
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = HBaseConfiguration.create()
    val job = Job.getInstance(conf, "Modify")
    job.setJarByClass(MapReduceModify.getClass)

    val scan = new Scan()
    TableMapReduceUtil.initTableMapperJob(MemberTable,
                                          scan,
                                          classOf[TheMapper],
                                          classOf[ImmutableBytesWritable],
                                          classOf[ImmutableBytesWritable],
                                          job)

    TableMapReduceUtil.initTableReducerJob("member", classOf[TheReducer], job)
    job.setNumReduceTasks(1)

    val b = job.waitForCompletion(true)
    if (!b) {
      throw new IOException("error with job!");
    }
  }
}
