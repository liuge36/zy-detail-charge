package com.csylh.blog_log


import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{AnalysisException, DataFrame, SaveMode, SparkSession}
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scalikejdbc.config.DBs

/**
  * Description:  博客系统的日志分析
  *
  * @Author: 留歌36
  * @Date: 2020/2/28 17:39
  */
object LiugeBlogLogAnalysis {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("LiugeBlogLogAnalysis")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(10))

    val topics = "liugeblog".split(",").toSet
    val kafkaParams = Map[String, String](
      "metadata.broker.list"->"120.27.243.91:9092",
      "auto.offset.reset" -> "smallest"
    )

    //  步骤一:先获取offset
    import scalikejdbc._
    /**
      * tuple 转 map
      * ().list.apply().toMap
      */
    DBs.setup()
    val fromOffsets =
      DB.readOnly {
        implicit session => {
          SQL("select * from offset_storage").map(rs =>
            (TopicAndPartition(rs.string("topic"), rs.int("partitions")),rs.long("offset"))
          ).list().apply()
        }
      }.toMap

    for (ele <- fromOffsets){
      println("读取MySQL偏移量相关数据==>topic:  " + ele._1.topic + "  partition:" + ele._1.partition +"  offset:"+ele._2)
    }

    // 步骤二: Direct 模式对接Kafka ,得到InputDStream
    val stream = if (fromOffsets.isEmpty){ // 从头消费

      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    }else { // 从指定偏移量进行消费

      val messageHandler = (mm:MessageAndMetadata[String,String]) => (mm.key(), mm.message())

      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams, fromOffsets, messageHandler)

    }

    // 步骤三: 业务逻辑 + 保存offset
    stream.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        println("~~~~~~~~~~~~~~~~~~~~~~~华丽的分割线~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        println(s"留歌本轮的统计结果：${rdd.count()}条数据")

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


        try {
          val accessRDD = rdd.map(line => {
            val splits = line._2.split(" ")
            val ip = splits(0)
            /**
              * 原始日志的第三个和第四个字段拼接起来就是完整的访问时间
              * [10/Nov/2016:00:01:02 +0800] ==> yyyy-MM-dd HH:mm:ss
              *
              */
            val time = splits(3) + " " + splits(4)

            val url = splits(6).replaceAll("\"","")
            //
            val traffic = splits(9)





            var referer = ""  // 考虑到脏数据
            try{
              referer = splits(10).replaceAll("\"","")  // 考虑到越界的问题
            }catch {
              case e:Exception => referer = ""
            }

            DateUtils.parse(time) + "\t" +  url + "\t" +  traffic +"\t" + ip + "\t" +  referer
          })
          // RDD ==> DF
//          accessRDD.foreach(println)
          var accessDF =  spark.createDataFrame(accessRDD
            .map(x => AccessConvertUtil.parseLog(x)),
            AccessConvertUtil.struct)

//          accessDF.printSchema()
//          accessDF.show()
          accessDF = accessDF.filter($"ip" =!= "")
          /**
            *
            * +--------------------+-------+------------------+---------------+----+-------------------+--------+
            * |                 url|traffic|           referer|             ip|city|               time|     day|
            * +--------------------+-------+------------------+---------------+----+-------------------+--------+
            * |                   /|  11490|               "-"|223.104.249.245|    |2020-02-28 19:14:14|20200228|
            * |/wp-content/theme...| 127182|"http://how2s.cn/"|223.104.249.245|    |2020-02-28 19:14:15|20200228|
            * |/wp-content/theme...| 137902|"http://how2s.cn/"|223.104.249.245|    |2020-02-28 19:14:15|20200228|
            * |/wp-content/theme...| 193778|"http://how2s.cn/"|223.104.249.245|    |2020-02-28 19:14:15|20200228|
            *
            */

         //    STEP3.1 : json中的ip转换一下  通过前面我们学习的Spark SQL UDF函数
            import org.apache.spark.sql.functions._

            def getLongIp() = udf((ip:String) => {
              IpUtils.ip2Long(ip)
            })

          accessDF = accessDF.withColumn("ip_long", getLongIp()($"ip"))

          // 方式1
          val resultDF: DataFrame = accessDF.join(ipRowDF,accessDF("ip_long")
                .between(ipRowDF("start_ip"),ipRowDF("end_ip")),"inner")
          resultDF.printSchema()
          resultDF.show()

          /**
            *
            * +--------------------+-------+------------------+---------------+-------------------+--------+----------+----------+----------+--------+----+----+
            * |                 url|traffic|           referer|             ip|               time|     day|   ip_long|  start_ip|    end_ip|province|city| isp|
            * +--------------------+-------+------------------+---------------+-------------------+--------+----------+----------+----------+--------+----+----+
            * |                   /|  11459|               "-"|223.104.249.245|2020-02-28 19:21:14|20200228|3748198901|3748198656|3748298751|    四川|成都|移动|
            * |/wp-content/theme...|  32556|"http://how2s.cn/"|223.104.249.245|2020-02-28 19:21:14|20200228|3748198901|3748198656|3748298751|    四川|成都|移动|
            * |/wp-content/theme...| 129387|"http://how2s.cn/"|223.104.249.245|2020-02-28 19:21:14|20200228|3748198901|3748198656|3748298751|    四川|成都|移动|
            *
            */


          // 这里将数据sink 到 MySQL表中去
          val tableName ="ods"

//          MySQLUtils.sink(resultDF,tableName)

          val url="jdbc:mysql://120.27.243.91:3306/onlineloganalysis?characterEncoding=utf-8&useSSL=false"
          val prop = new java.util.Properties
          prop.setProperty("driver", "com.mysql.jdbc.Driver")
          prop.setProperty("user", "root")
          prop.setProperty("password", "P@ssw0rd")


          resultDF.write.mode("append").jdbc(url, tableName, prop)


        }catch {
          case e:Exception => {

          }
        }


      }

      // 将Offset 提交到外部存储保存  <==   步骤二：保存offset
      // ======================保存offset的模板==================================
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      for (o <- offsetRanges) {
        if (o.fromOffset != o.untilOffset) {

          println("消费数据从多少到多少:" + s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
          DB.autoCommit {
            implicit session => {
              SQL("replace into offset_storage(topic,groupid,partitions,offset) values(?,?,?,?)")
                .bind(o.topic, "liuge.group.id", o.partition, o.untilOffset).update().apply()
            }
          }

        } else {
          println("!该批次没有消费到数据")
        }
      }
      // ========================================================
    })

    ssc.start()
    ssc.awaitTermination()

  }

}
