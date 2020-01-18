package com.csylh.kudu_liuge.business

import com.csylh.kudu_liuge.`trait`.IDataProcess
import com.csylh.kudu_liuge.utils.{KuduUtils, SQLUtils, SchemaUtils}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2020/1/17 11:08
  */
object ProvinceCityProcessor extends IDataProcess{

  override def process(spark: SparkSession, masterAddresses: String): Unit = {
    val sourceTableName = "ods"

    // 从KUDU的ods表中读取数据，然后进行按照省份和城市分组统计即可
    val odsDF: DataFrame = spark.read.format("org.apache.kudu.spark.kudu")
      .option("kudu.table", sourceTableName)
      .option("kudu.master", masterAddresses)
      .load()

//    odsDF.show(false)

    odsDF.createOrReplaceTempView("ods")
    val resultKUDU: DataFrame = spark.sql(SQLUtils.PROVINCE_CITY_SQL)
//    resultKUDU.show(false)

    val sinkTableName ="province_city_cnt"
    val partitionId = "provincename"

    // 这里将数据sink 到KUDU 表中去
    KuduUtils.sink(resultKUDU,sinkTableName, masterAddresses, SchemaUtils.ProvinceCitySchema, partitionId)


  }
}
