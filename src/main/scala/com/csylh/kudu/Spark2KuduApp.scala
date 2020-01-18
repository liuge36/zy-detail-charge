package com.csylh.kudu

import org.apache.spark.sql.SparkSession

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2020/1/15 11:12
  */
object Spark2KuduApp {
  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession.builder()
      .master("local[2]").getOrCreate()

    import spark.implicits._

    //    val config = ConfigFactory.load()
    //    val url = config.getString("db.default.url")
    //    val user = config.getString("db.default.user")
    //    val password = config.getString("db.default.password")
    //    val driver = config.getString("db.default.driver")
    //    val database = config.getString("db.default.database")
    //    val table = "wc"
    //
    //    val connectionProperties = new Properties()
    //    connectionProperties.put("user", user)
    //    connectionProperties.put("password", password)
    //
    //    // TODO... 以上代码是加载
    //
    //    // TODO... 就是你们需要开发的业务逻辑功能
    //    val jdbcDF: DataFrame = spark.read
    //      .jdbc(url, s"$database.$table", connectionProperties).filter($"cnt" > 11)


    // TODO... 以下代码是Sink
    val kuduMasters = "spark001"

    // 自定义数据如何实现    load  save
    //    jdbcDF.write.mode(SaveMode.Append).format("org.apache.kudu.spark.kudu")
    //      .option("kudu.master",kuduMasters)
    //      .option("kudu.table", "pk")
    //      .save()

    spark.read.format("org.apache.kudu.spark.kudu")
      .option("kudu.master",kuduMasters)
      .option("kudu.table", "app_stat")
      .load()
      .show()

    spark.stop()
  }

}
