package com.csylh.csv2mysql.utils

import java.sql.Connection

import com.mchange.v2.c3p0.ComboPooledDataSource

/**
  * Description: TODO
  *
  * @Author: 留歌36
  * @Date: 2019/9/17 13:38
  */
object MysqlPoolManager {

  // 单例模式生成 c3p0 数据库连接池
  var mysqlPool:MysqlPool = _
  synchronized{
    if (mysqlPool == null){
      mysqlPool = new MysqlPool
    }
    mysqlPool
  }

  class MysqlPool extends  Serializable{
    // 连接池的配置
    private val cbps:ComboPooledDataSource = new ComboPooledDataSource(true)

    cbps.setDriverClass("com.mysql.jdbc.Driver")
    cbps.setUser("db")
    cbps.setPassword("zdmedical")
    cbps.setJdbcUrl("jdbc:mysql://192.168.1.200:10081/demo_tnrmyy?useUnicode=true&characterEncoding=UTF-8&useSSL=false")

    // 取出连接
    def getConnection :Connection = {
      cbps.getConnection
    }
    // 关闭连接
    def getClose:Unit={
      cbps.close()
    }

  }
}
