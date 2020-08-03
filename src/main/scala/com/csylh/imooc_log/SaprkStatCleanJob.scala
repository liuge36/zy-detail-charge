package com.csylh.imooc_log

import org.apache.spark.sql.SparkSession

/**
  * Description: 完成数据清洗操作
  *
  * @Author: 留歌36
  * @Date: 2020/2/28 14:31
  */
object SaprkStatCleanJob {

  def main(args: Array[String]): Unit = {
    // 拿到spark
    val spark = SparkSession.builder()
      .appName("SparkStatFormatJob")
      .master("local[2]").getOrCreate()

    val accessRDD= spark.sparkContext.textFile("f://tmp//output//part-0*")
//    accessRDD.foreach(println)

    // RDD ==> DF
    val accessDF =  spark.createDataFrame(accessRDD
      .map(x => AccessConvertUtil.parseLog(x)),
      AccessConvertUtil.struct)

    accessDF.printSchema()
    accessDF.show(false)




    // 关闭spark
    spark.stop()
  }

}
