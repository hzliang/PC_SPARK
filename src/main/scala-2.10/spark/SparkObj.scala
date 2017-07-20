package spark

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ad on 2016/1/23.
  */
object SparkObj {
    val conf = new SparkConf().setAppName(ConfigSpk.taskName)
      .setMaster("local")
    //.setMaster("local")
    val ctx = new SparkContext(conf)
}
