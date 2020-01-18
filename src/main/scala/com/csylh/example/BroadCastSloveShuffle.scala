package com.csylh.example

/**
  * Description:  广播变量 + map 来解决数据倾斜的问题
  * 数据倾斜发生，Spark作业的performance会比期望差很多。
  *   数据倾斜调优，就是使用各种技术方案解决不同类型的数据倾斜问题，
  *   以保证Spark作业的性能。
  *
  * 在RDD中使用Join类操作or Spark SQL 中使用join语句时，join操作的一个rdd或表 的数据量比较小时【比如几百M或者一两G】。此方案适合
  *
  * @Author: 留歌36
  * @Date: 2019/10/24 19:10
  */
object BroadCastSloveShuffle {

}
