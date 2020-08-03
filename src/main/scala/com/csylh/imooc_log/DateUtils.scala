package com.csylh.imooc_log

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import org.apache.commons.lang3.time.FastDateFormat

/**
  * Description: 日期时间解析工具类
  *
  * 注意：SimpleDateFormat 是线程不安全的
  *
  * [10/Nov/2016:00:01:02 +0800]
  *
  *  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")  vs FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
  *
  * @Author: 留歌36
  * @Date: 2020/2/28 14:02
  */
object DateUtils {

  // 定义输入类型 ，输入格式
  val YYYYMMDDHHMM_TIME_FORMAT =  FastDateFormat.getInstance("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)

  // 定义target ,目标格式
  val TARGET_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  /**
    * 获取输入日志时间 long 类型
    * @param time
    * @return
    */
  def getTime(time:String) ={
    // [10/Nov/2016:00:01:02 +0800]
    try {
      YYYYMMDDHHMM_TIME_FORMAT
        .parse(time.substring(time.indexOf("[") + 1, time.lastIndexOf("]")))
        .getTime
    }catch {
      case e :Exception =>{
        0l
      }
    }

  }

  /**
    * 获取时间  yyyy-MM-dd HH:mm:ss
    * @param time
    */
  def parse(time:String) ={
    TARGET_FORMAT.format(new Date(getTime(time)))
  }

  def main(args: Array[String]): Unit = {
    println(parse("[10/Nov/2016:00:01:02 +0800]"))
  }



}
