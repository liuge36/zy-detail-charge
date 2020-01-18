package com.csylh.kudu_liuge.`trait`

import org.apache.spark.sql.SparkSession

/**
  * Description:  数据处理的接口
  *
  * @Author: 留歌36
  * @Date: 2020/1/16 11:18
  */
trait IDataProcess {

  def process(spark: SparkSession, masterAddresses: String)

}
