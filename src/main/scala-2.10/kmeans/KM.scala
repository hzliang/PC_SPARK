package kmeans

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD
import org.slf4j.{Logger, LoggerFactory}
import spark.{ClusterAccumulableParam, SparkObj}


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
    val clus = km(parsedData)
    val resRDD = clus.predict(parsedData).zip(parsedData)
    val output = master + args(1) //"/hzl/output/cluster"
    resRDD.saveAsTextFile(output)
    SparkObj.ctx.stop()
  }

  def km(data: RDD[Vector]) = {
    //聚类
    logger.info("set the num of classes is 2, start computer...")
    val clus = KMeans.train(data, ConfigKM.classCount, ConfigKM.itersTimes, ConfigKM.reRunTimes)
    clus.clusterCenters
    val resRDD = clus.predict(data).zip(data)
    clus.predict(data).groupBy(_).map(v)
    resRDD.groupBy(v => v._1).map(v => {
      val clusCount = v._2.size
      if (clusCount < ConfigKM.classDataNum) {
        val clusTmp = KMeans.train(data, clusCount / ConfigKM.classDataNum + 1, ConfigKM.itersTimes, ConfigKM.reRunTimes)
        clusTmp zip v._2
      }
      else {
        clusAcc += v._2.toArray
      }
    })
    clus
  }

  def kmeans(points: RDD[Vector]) = {


    System.err.println("Centroids changed by\n" +
      "\t   " + movement.map(d => "%3f".format(d)).mkString("(", ", ", ")") + "\n" +
      "\tto " + newCentroids.mkString("(", ", ", ")"))

    // Iterate if movement exceeds threshold
    if (movement.exists(_ > epsilon))
      kmeans(points, newCentroids, epsilon, sc)
    else
      return newCentroids
  }

  def belongTo(cente:)

}
