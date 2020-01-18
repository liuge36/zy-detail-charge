package com.csylh.kududemo.business

import com.csylh.kududemo.`trait`.DataProcess
import com.csylh.kududemo.utils._
import org.apache.spark.sql.{DataFrame, SparkSession}

object AreaStatProcessor extends DataProcess{
  override def process(spark: SparkSession): Unit = {
    val sourceTableName = DateUtils.getTableName("ods", spark)
    val masterAddresses = "hadoop000"

    val odsDF: DataFrame = spark.read.format("org.apache.kudu.spark.kudu") //
      .option("kudu.table", sourceTableName)
      .option("kudu.master", masterAddresses)
      .load()

    odsDF.createOrReplaceTempView("ods")

    val resultTmp: DataFrame = spark.sql(SQLUtils.AREA_SQL_STEP1)
    resultTmp.createOrReplaceTempView("area_tmp")
    
    val result: DataFrame = spark.sql(SQLUtils.AREA_SQL_STEP2)
//    result.show(false)

    val sinkTableName = DateUtils.getTableName("area_stat", spark)
    val partitionId = "provincename"

    KuduUtils.sink(result,sinkTableName,masterAddresses,SchemaUtils.AREASchema, partitionId)

  }
}
