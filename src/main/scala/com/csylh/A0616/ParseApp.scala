package com.csylh.A0616

import org.apache.spark.sql._
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

import scala.collection.mutable

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2020/6/16 9:21
  */
object ParseApp {

  def main(args: Array[String]): Unit = {

    val peopleSchema = StructType(Array(
      StructField("日期字段", StringType, nullable = true),
      StructField("其他字段", StringType, nullable = true)
      )
    )

    val spark =SparkSession.builder().master("local[4]").appName("ParseApp").getOrCreate()
    val inputDF = spark.read
      .format("com.crealytics.spark.excel")
//      .option("sheetName", "Sheet1")
      .option("header", "true")
      .schema(peopleSchema)
      .load("D:\\IDEA_Work\\zy-detail-charge\\src\\main\\scala\\com\\csylh\\A0616\\tt.xlsx")


    inputDF.show()

    var tableFiledArray =  getTableFiledArray(inputDF.select("日期字段"))

    for (tableFiled <- tableFiledArray){
      println(tableFiled)
    }








//    df.write
//      .format("com.crealytics.spark.excel")
////      .option("dataAddress", "'My Sheet'!B3:C35")
//      .option("header", "true")
//
////      .option("dateFormat", "yy-mmm-d") // Optional, default: yy-m-d h:mm
////      .option("timestampFormat", "mm-dd-yyyy hh:mm:ss") // Optional, default: yyyy-mm-dd hh:mm:ss.000
//      .mode("overwrite") // Optional, default: overwrite.
//      .save("2.xlsx")



    spark.stop()
  }

  def getTableFiledArray(t :DataFrame): mutable.Buffer[String] = {

    var returnTableFiledArray:mutable.Buffer[String] =  t.select("日期字段")
      .filter(!_.isNullAt(0)) // 去掉null
      .distinct() // 去掉本身重复的
      .as(Encoders.STRING).collect().toBuffer // Dataset[Row] ==> mutable.Buffer[String]

    // ================接下来就是一些Scala数组的操作================
    // delete elements from an Array or ArrayBuffer
    returnTableFiledArray -= ("为空")

    for ( i <- 0 until returnTableFiledArray.length){
      // 转大写
      returnTableFiledArray(i) = returnTableFiledArray(i).toUpperCase()
    }

    returnTableFiledArray.distinct

  }



}
