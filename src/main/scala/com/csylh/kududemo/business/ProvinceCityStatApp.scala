package com.csylh.kududemo.business

import com.csylh.kududemo.utils._
import org.apache.spark.sql.{DataFrame, SparkSession}

object ProvinceCityStatApp {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .master("local[2]").appName("ProvinceCityStatApp").getOrCreate()

    // 从KUDU的ods表中读取数据，然后进行按照省份和城市分组统计即可

    val sourceTableName = "ods"
    val masterAddresses = "hadoop000"

    val odsDF: DataFrame = spark.read.format("org.apache.kudu.spark.kudu") //
      .option("kudu.table", sourceTableName)
      .option("kudu.master", masterAddresses)
      .load()

    //odsDF.show(false)

    odsDF.createOrReplaceTempView("ods")
    val result: DataFrame = spark.sql(SQLUtils.PROVINCE_CITY_SQL)
    // result.show(false)


    val sinkTableName = "province_city_stat"
    val partitionId = "provincename"

    KuduUtils.sink(result,sinkTableName,masterAddresses,SchemaUtils.ProvinceCitySchema, partitionId)

    spark.stop()
  }
}
