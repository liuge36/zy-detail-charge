package com.csylh.kududemo.business

import com.csylh.kududemo.`trait`.DataProcess
import com.csylh.kududemo.utils._
import org.apache.spark.sql.{DataFrame, SparkSession}

object ProvinceCityStatProcessor extends DataProcess{
  override def process(spark: SparkSession): Unit = {
    // 从KUDU的ods表中读取数据，然后进行按照省份和城市分组统计即可

    val sourceTableName = DateUtils.getTableName("ods", spark)
    val masterAddresses = "hadoop000"

    val odsDF: DataFrame = spark.read.format("org.apache.kudu.spark.kudu") //
      .option("kudu.table", sourceTableName)
      .option("kudu.master", masterAddresses)
      .load()

    //odsDF.show(false)

    odsDF.createOrReplaceTempView("ods")
    val result: DataFrame = spark.sql(SQLUtils.PROVINCE_CITY_SQL)
    // result.show(false)


    val sinkTableName = DateUtils.getTableName("province_city_stat", spark)
    val partitionId = "provincename"

    KuduUtils.sink(result,sinkTableName,masterAddresses,SchemaUtils.ProvinceCitySchema, partitionId)

  }
}
