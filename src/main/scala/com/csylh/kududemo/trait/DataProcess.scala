package com.csylh.kududemo.`trait`

import org.apache.spark.sql.SparkSession

/**
  * 顶层数据处理接口
  */
trait DataProcess {
  def process(spark:SparkSession)
}
