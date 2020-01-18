package com.csylh.csv2mysql


import com.csylh.csv2mysql.utils.MysqlUtil
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Description:
  * SELECT dis_diag_comment
  * FROM ba_first_page1_doctor 437804
  *
  * To
  *
  * super_gidss_zy_address_mod  1706805
  *
  * @Author: 留歌36
  * @Date: 2019/10/31 16:55
  */
object MySQLColumn2MySQLApp {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("MySQLColumn2MySQLApp")
//      .master("local[2]")
      .getOrCreate()

    val inputDF:DataFrame = spark.read.format("jdbc")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("url", "jdbc:mysql://192.168.1.200:10081")
      .option("dbtable", "demo_tnrmyy.ba_first_page1_doctor")
      .option("user", "db")
      .option("password", "zdmedical")
      .load()
      .select("dis_diag_comment")

//    println(inputDF.count()) //437804
    val outputDF:DataFrame = spark.read.format("jdbc")
      .option("driver", "com.mysql.jdbc.Driver")
      .option("url", "jdbc:mysql://192.168.1.200:10081")
      .option("dbtable", "demo_tnrmyy.super_gidss_zy_address_mod")
      .option("user", "db")
      .option("password", "zdmedical")
      .load()

    outputDF.createOrReplaceTempView("result")
    val updateRDD: DataFrame = spark.sql("select * from result")

    if (!updateRDD.isEmpty){
//         println("==================update======================")
         //                updateRDD.show(false)
      MysqlUtil.update(updateRDD,inputDF)
    }


    spark.stop()
  }

}
