package com.csylh.kudu_liuge.business

import com.csylh.kudu_liuge.`trait`.IDataProcess
import com.csylh.kudu_liuge.utils.{KuduUtils, SQLUtils, SchemaUtils}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Description:  APP分布情况
  *
  * @Author: 留歌36
  * @Date: 2020/1/17 15:19
  */
object AppProcessor extends IDataProcess{
  override def process(spark: SparkSession, masterAddresses: String): Unit = {
    val sourceTableName = "ods"

    val odsDF: DataFrame = spark.read.format("org.apache.kudu.spark.kudu") //
      .option("kudu.table", sourceTableName)
      .option("kudu.master", masterAddresses)
      .load()

    odsDF.createOrReplaceTempView("ods")

    val resultTmp: DataFrame = spark.sql(SQLUtils.APP_SQL_STEP1)
    //resultTmp.show(false)
    resultTmp.createOrReplaceTempView("app_tmp")


    val resultKUDU: DataFrame = spark.sql(SQLUtils.APP_SQL_STEP2)
    resultKUDU.show(false)

    val sinkTableName = "app_stat"
    val partitionId = "appid"

    // 这里将数据sink 到KUDU 表中去
    KuduUtils.sink(resultKUDU,sinkTableName, masterAddresses, SchemaUtils.APPSchema, partitionId)



  }
}
