package kmeans

import java.net.URI

import kmeans.ConfigKM
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.Accumulator
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD
import org.slf4j.{Logger, LoggerFactory}
import spark.{StringAccumulableParam, ClusterAccumulableParam, SparkObj}


/**
  * Created by ad on 2016/1/23.
  */
object KM {
  val logger: Logger = LoggerFactory.getLogger(KM.getClass)

  def main(args: Array[String]) {
    val master = "hdfs://192.168.1.121:9000"
    val input = master + args(0) //"/hzl/input/cros3.csv"
    val data = SparkObj.ctx.textFile(input)
    val parsedData = data.map(s => Vectors.dense(s.split(',').map(_
      .toDouble))).cache()
    //    ConfigKM.totalDataCount = parsedData.count()
    val clus = kmeans(parsedData)
    //            val resRDD = clus.predict(parsedData).zip(parsedData)
    val output = master + args(1) //"/hzl/output/cluster"
    val hdfs = FileSystem.get(new URI(master), new Configuration())
    val outputFS = new Path(output)
    // 删除输出目录
    if (hdfs.exists(outputFS)) hdfs.delete(outputFS, true)
    clus.saveAsTextFile(output)
    SparkObj.ctx.stop()
  }

  def kmeans(data: RDD[Vector], classCount: Int): RDD[Vector] = {
    val clus = KMeans.train(data, classCount, ConfigKM.itersTimes, ConfigKM.reRunTimes)
    val resRDD = clus.predict(data).zip(data).persist()
    clus.predict(data).map((_, 1)).reduceByKey(_ + _).collect.foreach(v => {
      val newClass = resRDD.filter(v1 => {
        v1._1 == v._1
      }).map(_._2)
      if (v._2 >= ConfigKM.classDataNum) {
        kmeans(newClass, 2)
      } else {
        newClass
      }
    })
  }
}
