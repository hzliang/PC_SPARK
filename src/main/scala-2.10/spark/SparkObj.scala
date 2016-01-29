package spark

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ad on 2016/1/23.
  */
object SparkObj {
    val conf = new SparkConf(true).setAppName(ConfigSpk.taskName)
    val ctx = new SparkContext(conf)
}
