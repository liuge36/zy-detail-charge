package com.csylh.imooc_log

import org.apache.spark.sql.SparkSession

/**
  * Description:  第一步清洗：抽取出我们所需要的指定列的数据
  *
  * @Author: 留歌36
  * @Date: 2020/2/28 13:54
  */
object SparkStatFormatJob {
  def main(args: Array[String]): Unit ={

    // 拿到spark
    val spark = SparkSession.builder()
      .appName("SparkStatFormatJob")
      .master("local[2]").getOrCreate()


    val access = spark.sparkContext.textFile("f://10000_access.log")

//    access.foreach(println)
    access.map(line => {
      val splits = line.split(" ")

      val ip = splits(0)
      /**
        * 原始日志的第三个和第四个字段拼接起来就是完整的访问时间
        * [10/Nov/2016:00:01:02 +0800] ==> yyyy-MM-dd HH:mm:ss
        *
        */
      val time = splits(3) + " " + splits(4)

      val url = splits(11).replaceAll("\"","")

      val traffic = splits(9)

//      (ip, DateUtils.parse(time), url, traffic)

      DateUtils.parse(time) + "\t" + url + "\t" + traffic + "\t" + ip

    })
      .take(10).foreach(println)
    // 2016-11-10 00:01:02	http://www.imooc.com/code/1852	2345	117.35.88.11
//        .coalesce(1).saveAsObjectFile("f://tmp//output")







    // 关闭spark
    spark.stop()

  }

}
