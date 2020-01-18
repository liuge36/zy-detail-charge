package com.csylh.example

import org.apache.spark.{SparkConf, SparkContext}
import scala.util.Random


/**
  * 数据倾斜
  */
object Demo {
  System.setProperty("hadoop.home.dir", "c://hadoop")
  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("Demo").setMaster("local[2]")
    val sc=new SparkContext(conf)

    //准备数据
    val array=new Array[Int](10000)
    for (i <- 0 to 9999){
      array(i)=new Random().nextInt(10)
    }
    //array.foreach(x=>print(x+","))
    //生成一个rdd
    val rdd=sc.parallelize(array)
    //数据量很大就先取样
    //rdd.sample(false,0.1)
    //所有key加一操作
    val maprdd=rdd.map((_,1))
    //没有加随机前缀的结果
    maprdd.countByKey.foreach(print)

    //val wc=rdd.map(x=>(x,1)).reduceByKey(_+_)
    //wc.foreach(print)
    //(4,1016)(0,976)(6,959)(8,1022)(2,1051)(1,966)(3,1036)(7,973)(9,1004)(5,997)

    //两阶段聚合（局部聚合+全局聚合）处理数据倾斜

    //加随机前缀,文章评论有正确代码
    val prifix=new Random().nextInt(10)
    val prifixrdd=maprdd.map(x=>(prifix+"_"+x._1,x._2))

    //加上随机前缀的key进行局部聚合
    val tmprdd=prifixrdd.reduceByKey(_+_)
    //去除随机前缀
    val newrdd=tmprdd.map(x=> (x._1.split("_")(1),x._2))
    //最终聚合
    println()
    newrdd.reduceByKey(_+_).foreach(print)
  }
}