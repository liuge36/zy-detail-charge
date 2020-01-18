package com.csylh.csv2mysql.utils

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


  def update(df:DataFrame,inputDF:DataFrame) ={
    val inputArray = inputDF.collect()
    val l = inputArray.length

    // 拼接SQL 语句
    val sql = "update super_gidss_zy_address_mod set dis_diag_comment=? where id=?"

    df.foreachPartition(
      partitionOfRecords  => {
        // 每一个分区得到一个连接
        val conn = MysqlPoolManager.mysqlPool.getConnection

        // 自动提交关闭，因为要使用prepareStatement预处理的方式
        conn.setAutoCommit(false)
        val preparedStatement = conn.prepareStatement(sql)

        partitionOfRecords.foreach(records => {
          val i = records.getAs[Int]("id")

          val value = if ((i%l-1) <= 0){
              0
          }else{
            i%l-1
          }

          preparedStatement.setString(1,inputArray(value).mkString)

          preparedStatement.setInt(2, i)

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
