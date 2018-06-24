package com.nard.example.member

import java.io.{File, IOException}
import java.lang.Iterable

import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{TableMapReduceUtil, TableMapper}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.output.{FileOutputFormat, TextOutputFormat}
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}
import org.joda.time.{DateTime, Years}

import scala.math.BigDecimal.RoundingMode

/**
  * MapReduce example for computing the average age based on sex and output it onto a file
  */
object MapReduceToFile {

  class TheMapper extends TableMapper[Text, LongWritable] {
    type Context =
      Mapper[ImmutableBytesWritable, Result, Text, LongWritable]#Context
    override def map(key: ImmutableBytesWritable,
                     value: Result,
                     context: Context): Unit = {

      val bdate =
        Bytes.toLong(value.getValue(AttributeFamily, BirthdateQualifier))
      val age = Years.yearsBetween(new DateTime(bdate), DateTime.now()).getYears.toLong
      val sex = Bytes.toString(value.getValue(AttributeFamily, SexQualifier))

      val text = new Text()
      text.set(sex)

      context.write(text, new LongWritable(age))
    }
  }

  class TheReducer extends Reducer[Text, LongWritable, Text, LongWritable] {

    type Context = Reducer[Text, LongWritable, Text, LongWritable]#Context

    override def reduce(key: Text,
                        values: Iterable[LongWritable],
                        context: Context): Unit = {
      val it = values.iterator()
      var sum: Long = 0
      var counter: Int = 0
      while (it.hasNext) {
        val age = it.next().get()
        sum += age
        counter += 1
      }

      val totalAge = BigDecimal.valueOf(sum)
      val totalPeople = BigDecimal.valueOf(counter)
      val result =
        (totalAge / totalPeople).setScale(0, RoundingMode.HALF_UP).longValue()

      context.write(key, new LongWritable(result))
    }
  }

  val Directory = "/tmp/test"

  def main(args: Array[String]): Unit = {
    val conf = HBaseConfiguration.create()
    val job = Job.getInstance(conf, "AgeAverageBySex")
    job.setJarByClass(MapReduceToFile.getClass)

    val scan = new Scan()
    TableMapReduceUtil.initTableMapperJob(MemberTable,
                                          scan,
                                          classOf[TheMapper],
                                          classOf[Text],
                                          classOf[LongWritable],
                                          job)
    job.setReducerClass(classOf[TheReducer])
    job.setCombinerClass(classOf[TheReducer])
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[LongWritable])
    job.setOutputFormatClass(classOf[TextOutputFormat[Text, LongWritable]])
    job.setNumReduceTasks(1)

    val directory = new File(Directory)
    if (directory.exists()) {
      directory.renameTo(
        new File(s"${Directory}_${System.currentTimeMillis()}"))
    }
    FileOutputFormat.setOutputPath(job, new Path(Directory));

    val b = job.waitForCompletion(true)
    if (!b) {
      throw new IOException("error with job!");
    }
  }
}
