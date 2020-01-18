package com.csylh.example_liuge

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2019/10/29 8:46
  */
object WCDemoApp {

  def main(args:Array[String])={
    // conf
    val conf = new SparkConf().setAppName("WCDemoApp").setMaster("local[2]")

    // sc
    val sc = new SparkContext(conf)


    val text = sc.textFile("f:/ttt.txt",5)
//    text.collect().foreach(println)

    text.flatMap(x=>x.split(",")).map(x=>(x,1)).reduceByKey((x,y) => x+y).collect().foreach(println)


   while (true){
     Thread.sleep(1000000)
   }

    sc.stop()

  }
}
