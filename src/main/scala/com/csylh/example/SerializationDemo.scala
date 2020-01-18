package com.csylh.example

import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * Description: 
  *
  * @Author: 留歌36
  * @Date: 2019/10/24 14:10
  */
object SerializationDemo {
  System.setProperty("hadoop.home.dir", "c://hadoop")

  def main(args:Array[String]): Unit ={
    val conf = new SparkConf().setAppName("SerializationDemo").setMaster("local[2]")
      .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      .registerKryoClasses(Array(classOf[Student])) // 将自定义的类注册到Kryo

    val sc = new SparkContext(conf)

    val studentArr = new ArrayBuffer[Student]()

    for(i <- 1 to 1000000){
      studentArr += (Student(i + "", i + "a", 10, "male"))

    }

    val kryoSerializer = sc.parallelize(studentArr)
    kryoSerializer.persist(StorageLevel.MEMORY_ONLY_SER).count()

    while (true){
      Thread.sleep(1000)
    }

    sc.stop()
  }
  case class Student(id:String, name:String, age:Int, gender:String)

}



