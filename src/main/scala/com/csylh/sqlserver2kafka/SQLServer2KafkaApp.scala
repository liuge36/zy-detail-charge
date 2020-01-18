package com.csylh.sqlserver2kafka

import com.csylh.sqlserver2kafka.utils.MysqlUtil
import com.google.gson.Gson
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import scalikejdbc.config.DBs



/**
  * Description:
  *
  *
  *  SQLServer --> Kafka Connect  --> Kafka --> Spark --> MySQL(HBase/KUdu)
  *
  *  http://www.tracefact.net/tech/087.html
  *
  * @Author: 留歌36
  * @Date: 2019/9/24 18:42
  */
object SQLServer2KafkaApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("SQLServer2KafkaApp")
//      .master("local[2]")
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(10))

    val topics = "test1.dbo.t201909262".split(",").toSet
    val kafkaParams = Map[String, String](
      "metadata.broker.list"->"hadoop001:9093,hadoop001:9094,hadoop001:9095",
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
          SQL("select * from sqlserverkafka3").map(rs =>
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
        try{
          import spark.implicits._
          val jsonDS = rdd.map(_._2).toDS()
          val allDF:DataFrame = spark.read.json(jsonDS)
          //            allDF.show(false)
          //            allDF.printSchema()
          allDF.createOrReplaceTempView("allResult")

          val filterSql = "select * from allResult"
          val filterDF = spark.sql(filterSql)
          if (filterDF != null){
            filterDF.createOrReplaceTempView("result")
            try {
              val insertRDD: DataFrame = spark.sql("select after.* from result where op ='c' ")
              if (!insertRDD.isEmpty){
                // rdd -> MySQL
                //                println("==================insert======================")
                //                insertRDD.show(false)
                //                insertRDD.printSchema()
                // 表名 最后一个字段名 insertRDD
                MysqlUtil.insert("t20190926","ward_sn",insertRDD)
              }

              val updateRDD: DataFrame = spark.sql("select  after.* from result where op ='u' ")
              if (!updateRDD.isEmpty){
                //                println("==================update======================")
                //                updateRDD.show(false)
                MysqlUtil.update("t20190926","ward_sn","detail_sn",updateRDD)
              }
            }catch {
              case e:AnalysisException => {
                val deleteRDD: DataFrame = spark.sql("select before.detail_sn from result where op ='d' ")
                if (!deleteRDD.isEmpty){
                  //                    println("==================delete======================")
                  //                    deleteRDD.show(false)
                  MysqlUtil.delete("t20190926","detail_sn",deleteRDD)
                }
              }
            }
          }
        }catch {
          case e:Exception => e.printStackTrace()
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
              SQL("replace into sqlserverkafka3(topic,groupid,partitions,offset) values(?,?,?,?)")
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

  def handleMessage2CaseClass(jsonStr: String): ResultObject = {
    val gson = new Gson()
    gson.fromJson(jsonStr, classOf[ResultObject])
  }



  case class ResultObject(before:t201909262, after:t201909262, source:source, op:String, ts_ms:String)

  case  class  t201909262(patient_id:String,admiss_times:String, ledger_sn:String
                          , detail_sn:String,  occ_time:String,confirm_time:String,happen_date:String,charge_code:String,bill_item_code:String,audit_code:String,
                          orig_price:String,charge_price:String,charge_amount:String,charge_group:String,apply_opera:String,confirm_opera:String,
                          charge_status:String, infant_flag:String ,self_flag:String,separate_flag:String,suprice_flag:String,drug_flag:String,emergency_flag:String,
                          ope_flag:String,apply_status:String, pay_self:String,serial:String,ward_sn:String,dept_sn:String,order_no:String,exec_unit:String,group_no:String,
                          page_no:String,page_type:String, report_date:String,adt_dept_no:String,back_flag:String,orig_dept:String,orig_ward:String,cash_date:String,
                          account_date:String,doctor_code:String, samp_barcode:String,occ_page_no:String,fit_flag:String,samp_type:String,samp_id:String,charge_fee:String,
                          parent_detail_sn:String,parent_page_no:String, exec_opid:String,exec_time:String,exec_status:String,apply_doctor:String,flag:String,buy_price:String,dt:String)

  case class source(version:String,connector:String,name:String,ts_ms:String,change_lsn:String,commit_lsn:String,snapshot:String)


}



