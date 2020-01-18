package com.csylh.example

import java.io.File

import org.apache.spark.SparkFiles
import org.apache.spark.sql.SparkSession

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2019/10/24 8:41
  */
object SparkRemoteFileTest {
  System.setProperty("hadoop.home.dir", "c://hadoop")
  def main(args:Array[String]): Unit ={
    if (args.length < 1){
      System.err.println("Usage: SparkRemoteFileTest <file>")
      System.exit(1)
    }
    val spark = SparkSession.builder()
      .appName("SparkRemoteFileTest")
      .master("local[2]")
      .getOrCreate()


    val sc = spark.sparkContext


//    sc.textFile(args(0)).collect().foreach(println)

    val rdd = sc.parallelize(Seq(1)).map(_ => {
      val localLocation = SparkFiles.get(args(0))
      println(s"${args(0)} is stored at : $localLocation")
      new File(localLocation).isFile
    })

    val trunkCheck = rdd.collect().head
    println(s"Mounting of ${args(0)} was ${trunkCheck}")

    spark.stop()
  }

}
