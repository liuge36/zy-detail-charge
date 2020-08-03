package com.csylh.blog_log

import org.apache.spark.sql.{DataFrame, SaveMode}

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2020/2/28 19:24
  */
object MySQLUtils {
  /**
    * sink 到MySQL表中去
    * @param data
    * @param tableName
    */
  def sink(data: DataFrame, tableName:String): Unit ={
    // 数据写入KUDU
    data.write.mode(SaveMode.Append)
      .format("org.apache.kudu.spark.kudu")

      .save()
  }



}
