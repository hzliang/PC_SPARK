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

import scala.collection.mutable.ArrayBuffer


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
    ConfigKM.totalDataCount = parsedData.count()
    val clus = kmeans(parsedData, ConfigKM.classCount)
    //            val resRDD = clus.predict(parsedData).zip(parsedData)
    val output = master + args(1) //"/hzl/output/cluster"
    val hdfs = FileSystem.get(new URI(master), new Configuration())
    val outputFS = new Path(output)
    // 删除输出目录
    if (hdfs.exists(outputFS)) hdfs.delete(outputFS, true)
    clus.saveAsTextFile(output)
    SparkObj.ctx.stop()
  }

  def kmeans(dataNeedCluster: RDD[Vector], clusterCount: Int): RDD[(Int, Vector)] = {
    val clus = KMeans.train(dataNeedCluster, clusterCount, ConfigKM.itersTimes, ConfigKM.reRunTimes)
    val preditRDD = clus.predict(dataNeedCluster)
    val resRDD = preditRDD.zip(dataNeedCluster).persist()
    val rddArray = new ArrayBuffer[RDD[(Int, Vector)]]()
    preditRDD.map((_, 1)).reduceByKey(_ + _).collect().foreach(clusterInfo => {
      val classData = resRDD.filter(currCluster => {
        currCluster._1 == clusterInfo._1
      })
      if (clusterInfo._2 >= ConfigKM.classDataNum) {
        kmeans(classData.map(_._2), clusterInfo._1 / ConfigKM.classDataNum + 1)
      } else {
        rddArray.append(classData)
      }
    })
    rddArray.reduce(_ union _)
  }
}
