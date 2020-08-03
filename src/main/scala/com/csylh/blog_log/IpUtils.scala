package com.csylh.blog_log

/**
  * Description:
  *
  * @Author: 留歌36
  * @Date: 2020/1/16 11:49
  */
object IpUtils {
  /**
    * 将字符串转成十进制
    * @param ip
    */
  def ip2Long(ip:String) = {


    val splits: Array[String] = ip.split("[.]")
    var ipNum = 0l

    for(i <- 0 until(splits.length) ) {
      ipNum = splits(i).toLong | ipNum << 8L
      //      println(i+":"+ipNum)
    }

    ipNum
  }

  def main(args: Array[String]): Unit = {

    println(ip2Long("182.91.190.221"))
    //println(91 | 182 << 8) 46683
    //    <<	左移动运算符	60 << 2 输出结果 240 ，二进制解释： 1111 0000
    // |  按位或 ：有一则1    。   &	按位与 ： 同时为1 则1

  }


}
