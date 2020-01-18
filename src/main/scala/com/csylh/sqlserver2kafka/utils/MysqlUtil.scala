package com.csylh.sqlserver2kafka.utils

import org.apache.spark.sql.DataFrame

/**
  * Description: MySQL操作工具类
  *
  * <h1> 封装好 新增 修改 删除 的DML语句</h1>
  *
  * @Author: 留歌36
  * @Date: 2019/9/17 13:51
  */
object MysqlUtil {


  /**
    *
    * @param tableName
    * @param lastColumn
    * @param df
    */
  def insert(tableName:String,lastColumn:String, df:DataFrame) ={

    // 拼接SQL 语句
    val cols = df.columns

    var columns = "("
    for(i <- cols.toList){
      if (!lastColumn.equals(i)){
        columns += i + ", "
      }else{
        columns += i +") "
      }
    }

    var sql = "insert into " +tableName+columns+" values("

    for(i <- 1 to cols.length){
      sql += " ?"
      if (i!=cols.length){
        sql +=" ,"
      }
    }
    sql += ")"

    // 遍历DataFrame分区
    df.foreachPartition(
      partitionOfRecords  => {

        // 每一个分区得到一个连接
        val conn = MysqlPoolManager.mysqlPool.getConnection

        // 自动提交关闭，因为要使用prepareStatement预处理的方式
        conn.setAutoCommit(false)
        val preparedStatement = conn.prepareStatement(sql)

        partitionOfRecords.foreach(records => {
          for (i <- 1 to cols.length){
            preparedStatement.setString(i,records.getAs[String](i-1))
          }
          preparedStatement.addBatch()
        })

        preparedStatement.executeBatch()
        conn.commit()
        preparedStatement.close()
        conn.close()
      }
    )
  }

  /**
    *
    * @param tableName
    * @param lastColumn
    * @param primaryKey
    * @param df
    */
  def update(tableName:String,lastColumn:String,primaryKey:String, df:DataFrame) ={

    // 拼接SQL 语句
    var sql = "update " +tableName+" set "
    val cols = df.columns
    var columns = " "
    for(i <- cols.toList){
      if (!lastColumn.equals(i)){
        columns += i + "=?, "
      }else{
        columns += i + "=? "
      }
    }
    sql = sql + columns +" where " + primaryKey +"=?"


    df.foreachPartition(
      partitionOfRecords  => {
        // 每一个分区得到一个连接
        val conn = MysqlPoolManager.mysqlPool.getConnection

        // 自动提交关闭，因为要使用prepareStatement预处理的方式
        conn.setAutoCommit(false)
        val preparedStatement = conn.prepareStatement(sql)

        partitionOfRecords.foreach(records => {

          for (i <- 1 to cols.length){
            preparedStatement.setString(i,records.getAs[String](i-1))
          }

          preparedStatement.setString(cols.length+1,records.getAs[String](primaryKey))

          preparedStatement.addBatch()
        })
        preparedStatement.executeBatch()

        conn.commit()
        preparedStatement.close()
        conn.close()
      }
    )
  }

  /**
    *
    * @param tableName
    * @param primaryKey
    * @param df
    */
  def delete(tableName:String,primaryKey:String, df:DataFrame) ={

    // 拼接SQL 语句
    val sql = "delete from " +tableName +" where "+ primaryKey+"=?"

    // 遍历DataFrame分区
    df.foreachPartition(
      partitionOfRecords  => {
        // 每一个分区得到一个连接
        val conn = MysqlPoolManager.mysqlPool.getConnection
        // 自动提交关闭，因为要使用prepareStatement预处理的方式
        conn.setAutoCommit(false)

        val preparedStatement = conn.prepareStatement(sql)

        partitionOfRecords.foreach(records => {
          preparedStatement.setString(1,records.getAs[String](primaryKey))

          preparedStatement.addBatch()
        })

        preparedStatement.executeBatch()

        conn.commit()
        preparedStatement.close()
        conn.close()
      }
    )
  }
}
