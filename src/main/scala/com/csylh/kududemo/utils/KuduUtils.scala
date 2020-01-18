package com.csylh.kududemo.utils

import java.util

import org.apache.kudu.Schema
import org.apache.kudu.client.KuduClient.KuduClientBuilder
import org.apache.kudu.client.{CreateTableOptions, KuduClient}
import org.apache.spark.sql.{DataFrame, SaveMode}

object KuduUtils {


  /**
    * 将DF数据落地到Kudu
    * @param data  DataFrame结果集
    * @param tableName  Kudu目标表
    * @param master Kudu的Master地址
    * @param schema Kudu表的schema信息
    * @param partitionId Kudu表的分区字段
    */
  def sink(data: DataFrame,
           tableName:String,
           master: String,
           schema: Schema,
           partitionId:String
          ): Unit = {
    val client: KuduClient = new KuduClientBuilder(master).build()

    val options: CreateTableOptions = new CreateTableOptions()
    options.setNumReplicas(1)
    val parcols: util.LinkedList[String] = new util.LinkedList[String]()
    parcols.add(partitionId)
    options.addHashPartitions(parcols,3)

    // 创建表
    if(client.tableExists(tableName)) {
      client.deleteTable(tableName)
    }
    client.createTable(tableName,schema,options)

    // 数据写入KUDU
    data.write.mode(SaveMode.Append)
      .format("org.apache.kudu.spark.kudu")
      .option("kudu.table",tableName)
      .option("kudu.master",master)
      .save()
  }

}
