package com.csylh.kudu

import java.util

import org.apache.kudu.{ColumnSchema, Schema, Type}
import org.apache.kudu.client._

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2020/1/14 10:53
  */
object KuduAPIApp{



  def main(args:Array[String]): Unit ={

    val KUDU_MASTERS = "spark001"

    val client:KuduClient = new KuduClient.KuduClientBuilder(KUDU_MASTERS)
      .defaultAdminOperationTimeoutMs(30000 *2)
      .defaultOperationTimeoutMs(30000 *2)
      .defaultSocketReadTimeoutMs(10000 *2)
      .build()

    val tableName = "lh"
    val tableNames = "ff"

//    createTable(client,tableName)

//    deleteTable(client,tableName)

    deleteTableAll(client,tableNames)

//    insertRows(client,tableName)
//
//
//    query(client,tableName)

//    updateRow(client, tableName)
//
//    query(client,tableName)

//    val newTableName = "newl_lh"
//    renameTable(client, tableName, newTableName)

    client.close()

  }

  /**
    * 插入数据
    *
    * 作业：批量数据插入
    * @param client
    * @param tableName
    * @return
    */
  def insertRows(client: KuduClient, tableName: String) = {
    val table:KuduTable = client.openTable(tableName)  // 根据表名获取kudu的表
    val session:KuduSession = client.newSession()
    // 基于session进行操作
    for (i <- 1 to 10){

      val insert:Insert = table.newInsert()
      val row:PartialRow =  insert.getRow
      row.addString("word",s"lh-$i")
      row.addInt("cnt",100+i)

      session.apply(insert)

    }

  }


  /**
    * 创建表
    * @param client
    * @param tableName
    */
  def createTable(client: KuduClient, tableName: String): Unit = {

    import scala.collection.JavaConverters._
    val columns = List(
      new ColumnSchema.ColumnSchemaBuilder("word",Type.STRING).key(true).build(),
      new ColumnSchema.ColumnSchemaBuilder("cnt",Type.INT32).build()
    ).asJava

    val schema = new Schema(columns)

    val options = new CreateTableOptions()
    options.setNumReplicas(1) // 设置副本为1 ，因为我们的kudu只有一个节点

    val parcols:util.ArrayList[String] = new util.ArrayList[String]()
    parcols.add("word")
    options.addHashPartitions(parcols,3) // 按照 “word”  进行分桶

    client.createTable(tableName, schema, options)

  }

  /**
    * 删除表
    * @param client
    * @param tableName
    */
  def deleteTable(client: KuduClient, tableName: String) = {
      client.deleteTable(tableName)
  }

  /**
    * 删除多张表
    * @param client
    * @param tableNames
    */
  def deleteTableAll(client: KuduClient, tableNames: String) = {
    val splits = tableNames.split(",")
    splits.map(x=>{
      deleteTable(client,x)
    })

  }


  /**
    * 数据查询
    * @param client
    * @param tableName
    */
  def query(client: KuduClient, tableName: String) = {

    val table:KuduTable = client.openTable(tableName)

    val scanner:KuduScanner = client.newScannerBuilder(table).build()

    while (scanner.hasMoreRows){

      val rowResultIterator:RowResultIterator = scanner.nextRows()

      while (rowResultIterator.hasNext){
        val result:RowResult = rowResultIterator.next()
        println(result.getString("word")
          + "===>" +
          result.getInt("cnt"))
      }

    }
  }

  /**
    * 修改某行数据
     * @param client
    * @param tableName
    */
  def updateRow(client: KuduClient, tableName: String) = {
    val table:KuduTable = client.openTable(tableName)
    val session:KuduSession = client.newSession()

    val update:Update = table.newUpdate()
    val row:PartialRow = update.getRow
    row.addString("word","lh-1")
    row.addInt("cnt",999999)
    session.apply(update)
//    session.close()

  }

  /**
    * 修改表名
    * @param client
    * @param tableName
    * @param newTableName
    */
  def renameTable(client: KuduClient, tableName: String, newTableName: String) = {

    val options:AlterTableOptions = new AlterTableOptions()
    options.renameTable(newTableName)

    client.alterTable(tableName,options)
  }






}
