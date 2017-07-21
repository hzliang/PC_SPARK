package kmeans

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.Accumulator
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.slf4j.{Logger, LoggerFactory}
import spark.SparkObj

import scala.collection.mutable.ArrayBuffer


/**
  * Created by ad on 2016/1/23.
  */
object KM {
  val logger: Logger = LoggerFactory.getLogger(KM.getClass)

  def main(args: Array[String]) {
    val master = "hdfs://192.168.1.121:9000"
    val input = master + "/hzl/input/test/20000sin3.csv" //"/hzl/input/SKEffectTest.csv"
    val data = SparkObj.ctx.textFile(input)
    val parsedData = data.map(s => Vectors.dense(s.split(',').map(_
      .toDouble))).cache()
    ConfigKM.totalDataCount = parsedData.count()
    //    val clus = KMeans.train(parsedData, ConfigKM.classCount,
    //      ConfigKM.itersTimes, ConfigKM.reRunTimes).predict(parsedData).
    //      map((_, 1)).reduceByKey(_ + _)
    val accum = SparkObj.ctx.accumulator(ConfigKM.classCount, "My Accumulator")
    val rddArray = new ArrayBuffer[RDD[(Int, Vector)]]()
    kmeans(parsedData, ConfigKM.classCount, ConfigKM.classCount, rddArray, false)
    val clus = rddArray.reduce(_ union _)
    //            val resRDD = clus.predict(parsedData).zip(parsedData)
    //    val output = master + args(1) //"/hzl/output/cluster"
    //    val hdfs = FileSystem.get(new URI(master), new Configuration())
    //    val outputFS = new Path(output)
    //    // 删除输出目录
    //    if (hdfs.exists(outputFS)) hdfs.delete(outputFS, true)
    val output = "/home/hy/cluster"
    clus.repartition(1).saveAsTextFile(output)
    SparkObj.ctx.stop()
  }

  /**
    * 循环使用kmeans聚类，直到结果符合数量要求
    * 即每个类数据的数量小于阈值
    *
    * @param dataNeedCluster 需要聚类的数据
    * @param initCluCount    初始聚类数量
    * @param rddArray        保存RDD数据
    * @param flag            是否是对子类聚类
    */
  def kmeans(dataNeedCluster: RDD[Vector], initCluCount: Int, totalCluCount: Int,
             rddArray: ArrayBuffer[RDD[(Int, Vector)]], flag: Boolean): Unit = {
    val clu = KMeans.train(dataNeedCluster, initCluCount, ConfigKM.itersTimes, ConfigKM.reRunTimes)
    val preRDD = clu.predict(dataNeedCluster)
    val resRDD = preRDD.zip(dataNeedCluster)//.persist()
    preRDD.map((_, 1)).reduceByKey(_ + _).collect().foreach(clusterInfo => {
      val classData = resRDD.filter(currCluster => {
        currCluster._1 == clusterInfo._1
      })
      if (clusterInfo._2 >= 1.25 * ConfigKM.classDataNum) {
        val tmp = math.ceil((clusterInfo._2+0.0) / ConfigKM.classDataNum).toInt
        kmeans(classData.map(_._2), tmp, totalCluCount + tmp, rddArray, true)
      } else {
        if (flag) {
          rddArray.append(classData.map(v => {
            (v._1 + totalCluCount, v._2)
          }))
        }
        else {
          rddArray.append(classData)
        }
      }
    })
  }
}
