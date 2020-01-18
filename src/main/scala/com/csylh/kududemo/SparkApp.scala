package com.csylh.kududemo

import com.csylh.kududemo.business._
import org.apache.commons.lang3.StringUtils
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

/**
  * 整个项目Spark作业的入口点
  *
  * 离线的处理 一天一个批次
  */
object SparkApp extends Logging{

  def main(args: Array[String]): Unit = {

    // .master("local[2]").appName("SparkApp")   Spark官网强调不要硬编码，appName master统一使用spark-submit提交的时候指定即可
    val spark: SparkSession = SparkSession.builder().getOrCreate()

    /**
      * 入参统计：
      * 1） spark.time
      * 2） spark.raw.path
      * 3） spark.ip.path
      */

    // spark-submit ......  --conf spark.time=20181007
    val time = spark.sparkContext.getConf.get("spark.time")  // spark框架只认以spark.开头的参数，否则系统不识别
    if(StringUtils.isBlank(time)) {  // 如果是空，后续的代码就不应该执行了
      logError("处理批次不能为空....")
      System.exit(0)
    }

    // STEP1: ETL
    LogETLProcessor.process(spark)

    // STEP2：省份地市统计
    ProvinceCityStatProcessor.process(spark)

    // STEP3: 地域分布情况统计
     AreaStatProcessor.process(spark)

    // STEP4: APP分布情况统计
    AppStatProcessor.process(spark)
    spark.stop()
  }

}
