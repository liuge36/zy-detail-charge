package com.csylh.kudu_liuge.business

import com.csylh.kudu_liuge.`trait`.IDataProcess
import com.csylh.kudu_liuge.utils.{IpUtils, KuduUtils, SQLUtils, SchemaUtils}
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Description:  源数据的 ETL 操作，最后将数据落地到KUDU中去
  *
  * @Author: 留歌36
  * @Date: 2020/1/16 11:18
  */
object LogETLProcessor extends IDataProcess with Logging{
  override def process(spark: SparkSession, masterAddresses: String): Unit = {

    // STEP1 : 将原始json数据通过 spark Data Source API 直接读取进来 ，加载为DF
    var jsonDF: DataFrame = spark.read.json("D:\\IDEA_Work\\zy-detail-charge\\data\\data-test.json")
//    jsonDF.show(false)

    // STEP2: 将ip.txt 文件加载进来
    val ipRowRDD: RDD[String] = spark.sparkContext.textFile("D:\\IDEA_Work\\zy-detail-charge\\data\\ip.txt")
    import spark.implicits._
    val ipRowDF:  DataFrame =ipRowRDD.map(x => {
      val splits: Array[String] = x.split("\\|")
      val startIP: Long = splits(2).toLong
      val endIP: Long = splits(3).toLong
      val province: String = splits(6)
      val city: String = splits(7)
      val isp: String = splits(9)
      (startIP, endIP, province, city, isp)
    }).toDF("start_ip", "end_ip", "province", "city", "isp")
//    ipRowDF.show(false)
    /**
      * +--------+--------+--------+----+------------------+
      * |start_ip|end_ip  |province|city|isp               |
      * +--------+--------+--------+----+------------------+
      * |16777472|16778239|福建    |福州|电信              |
      */

    // STEP3: 需要将每一行日志中的ip获得到对应的省份、城市、运营商
    // 两个DF(jsonDF,ipRowDF)进行join，条件是json中的ip 是在规则ip中的范围内就行 ip between ... and ...
//      def join(right: Dataset[_], joinExprs: Column): DataFrame = join(right, joinExprs, "inner")
    /**
      * left join :左表(A)的记录将会全部表示出来,而右表(B)只会显示符合搜索条件的记录 B表记录不足的地方均为NULL
      * left join :右表(B)为基础的,A表不足的地方用NULL填充
      * inner join ： A,B表都有数据才行
      */
    //   STEP3.1 : json中的ip转换一下  通过前面我们学习的Spark SQL UDF函数
    import org.apache.spark.sql.functions._

    def getLongIp() = udf((ip:String) => {
      IpUtils.ip2Long(ip)
    })

    jsonDF = jsonDF.withColumn("ip_long", getLongIp()($"ip"))

    // 方式1
//    val resultKUDU: DataFrame = jsonDF.join(ipRowDF,jsonDF("ip_long")
//      .between(ipRowDF("start_ip"),ipRowDF("end_ip")),"inner")
//    resultKUDU.show(false)

    // 方式2
    jsonDF.createOrReplaceTempView("logs")
    ipRowDF.createOrReplaceTempView("ips")

    // ETL处理完之后，肯定要落地到某个地方 KUDU
    val sql = SQLUtils.SQL
    val resultKUDU: DataFrame = spark.sql(sql)
//    resultKUDU.show(false)

    // STEP4 . ETL处理完之后，肯定要落地到某个地方 KUDU
    // 只需要定义表相关的信息，剩下的创建表 删除表操作全部封装到KuduUtils的Sink方法中
    val tableName ="ods"
    val partitionId = "ip"

    // 这里将数据sink 到KUDU 表中去
    KuduUtils.sink(resultKUDU,tableName,masterAddresses,SchemaUtils.ODSSchema, partitionId)



  }
}
