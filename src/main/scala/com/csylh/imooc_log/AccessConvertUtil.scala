package com.csylh.imooc_log

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
      StructField("url", StringType),
      StructField("cmsType", StringType),
      StructField("cmsId", LongType),
      StructField("traffic", LongType),
      StructField("city", StringType),
      StructField("time", StringType),
      StructField("day", StringType)
    )
  )

  /**根据输入的每一行记录 转换为输出
    *
    * @param log
    */
  def parseLog(log:String)={
    try {
      val splits = log.split("\t")

      val url = splits(1)
      val traffic = splits(2).toLong
      val ip =splits(3)

      val domain = "http://www.imooc.com/"
      val cms = url.substring(url.indexOf(domain) + domain.length)
      val cmsTypeId = cms.split("/")
      var cmsType = ""
      var cmsId = 0L
      if (cmsTypeId.length > 1){
        cmsType = cmsTypeId(0)
        cmsId = cmsTypeId(1).toLong
      }

      val city = ""
      val time = splits(0)
      val day = time.substring(0,10).replaceAll("-","")

      // 这个里面的字段要和结构体里面的对应上
      Row(url, cmsType, cmsId, traffic, ip, city, time, day)

    }catch {
      case e :Exception => Row("", "", 0l, 0l, "", "", "", "")
    }

  }

}
