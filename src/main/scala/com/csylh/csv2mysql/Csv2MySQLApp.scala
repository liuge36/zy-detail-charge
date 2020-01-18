package com.csylh.csv2mysql

import com.csylh.csv2mysql.utils.MysqlPoolManager
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2019/10/30 17:53
  */
object Csv2MySQLApp {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Csv2MySQLApp")
//      .master("local[5]")
      .getOrCreate()

    val lines:DataFrame = spark.read
//      .option("header","true")
      .csv("file:///root/zy-data/view_zy_detail_charge_tj_bak_0_0.csv")

//    lines.show(1,false)
//    |_c0         |_c1|_c2|_c3     |_c4                          |_c5                          |_c6                          |_c7   |_c8|_c9  |_c10|_c11|_c12|_c13|_c14 |_c15 |_c16|_c17|_c18|_c19|_c20|_c21|_c22|_c23|_c24|_c25|_c26|_c27   |_c28   |_c29    |_c30   |_c31  |_c32|_c33|_c34                         |_c35|_c36|_c37   |_c38   |_c39|_c40|_c41 |_c42|_c43|_c44|_c45|_c46|_c47|_c48|_c49|_c50|_c51               |_c52|_c53|_c54|_c55|_c56    |


    // 拼接SQL 语句
    val cols = lines.columns


    var sql = "insert into view_zy_detail_charge_tj values("

    for(i <- 1 to cols.length){
      sql += " ?"
      if (i!=cols.length){
        sql +=" ,"
      }
    }


    sql += ")"


    lines.foreachPartition(partitionOfRecords => {
      println("=========1==========")

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

    })

    spark.stop()
  }

}
