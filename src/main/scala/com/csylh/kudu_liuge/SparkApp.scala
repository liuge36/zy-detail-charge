package com.csylh.kudu_liuge

import com.csylh.kudu_liuge.business.{AppProcessor, AreaProcessor, LogETLProcessor, ProvinceCityProcessor}
import org.apache.spark.sql.SparkSession

/**
  * Description:  算是程序启动类：整个spark作业的入口点
  *
  * @Author: 留歌36
  * @Date: 2020/1/16 11:12
  */
object SparkApp {
  def main(args: Array[String]): Unit ={

    // 构造需要的参数
    val masterAddresses: String = "spark001"
    val spark: SparkSession = SparkSession.builder()
      .master("local[2]")
      .appName("SparkApp")
      .getOrCreate()

    // STEP1: ETL
//    LogETLProcessor.process(spark, masterAddresses)

    // STEP2：省份地市统计
//    ProvinceCityProcessor.process(spark, masterAddresses)

    // STEP3: 地域分布情况统计
//    AreaProcessor.process(spark, masterAddresses)

    // STEP4: APP分布情况统计
    AppProcessor.process(spark, masterAddresses)



    spark.stop()
  }

}
