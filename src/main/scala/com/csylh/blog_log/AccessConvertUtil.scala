package com.csylh.blog_log

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}

/**
  * Description:  访问日志转换工具类
  *
  * @Author: 留歌36
  * @Date: 2020/2/28 14:37
  */
object AccessConvertUtil {

  // 定义输出字段的类型结构
  val struct = StructType(
    Array(
      StructField("url",StringType),

      StructField("traffic",StringType),
      StructField("referer",StringType),

      StructField("ip",StringType),
      StructField("time",StringType),
      StructField("day",StringType)
    )
  )

  /**根据输入的每一行记录 转换为输出
    *
    * @param log
    */
  def parseLog(log:String)={
    try {

      // 2020-02-28 18:53:59	/wp-content/themes/kratos-master/static/images/thumb/47.jpg	158337	223.104.249.245	"http://how2s.cn/"
      val splits = log.split("\t")

      val url = splits(1)
      val traffic = splits(2)
      val ip =splits(3)


      val referer = splits(4)

      val time = splits(0)
      val day = time.substring(0,10).replaceAll("-","")

      // 这个里面的字段要和结构体里面的对应上
      Row(url, traffic, referer, ip, time, day)

    }catch {
      case e :Exception => Row("", "", "", "", "", "")
    }

  }

}
