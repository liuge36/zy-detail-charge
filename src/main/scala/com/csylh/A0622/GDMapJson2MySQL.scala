package com.csylh.A0622

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

import scala.util.parsing.json.JSON
/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2020/6/24 16:43
  */
object GDMapJson2MySQL {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("GDMapJson2MySQL")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext

    //定义schema
    val citySchema = new StructType()
      .add("citycode", ArrayType(StringType),nullable = true)
      .add("adcode", StringType,nullable = true)
      .add("name", StringType,nullable = true)
      .add("center", StringType,nullable = true)
      .add("level", StringType,nullable = true) // 国家
      .add("districts", ArrayType( new StructType()
        .add("citycode", ArrayType(StringType),nullable = true)
        .add("adcode", StringType,nullable = true)
        .add("name", StringType,nullable = true)
        .add("center", StringType,nullable = true)
        .add("level", StringType,nullable = true) // 省
        .add("districts",ArrayType( new StructType()
          .add("citycode", StringType,nullable = true)
          .add("adcode", StringType,nullable = true)
          .add("name", StringType,nullable = true)
          .add("center", StringType,nullable = true)
          .add("level", StringType,nullable = true) // 市
          .add("districts",ArrayType( new StructType()
            .add("citycode", StringType,nullable = true)
            .add("adcode", StringType,nullable = true)
            .add("name", StringType,nullable = true)
            .add("center", StringType,nullable = true)
            .add("level", StringType,nullable = true) // 区县
              .add("districts", ArrayType(StringType),nullable = true)

            ))))
        )
      )


    val gdDF = spark.read
      .schema(citySchema)
      .json("D:\\IDEA_Work\\zy-detail-charge\\src\\main\\scala\\com\\csylh\\A0622\\2020-06-23高德行政区域API")

    gdDF.printSchema()
    gdDF.show(1,false)







    spark.stop()



  }

}
