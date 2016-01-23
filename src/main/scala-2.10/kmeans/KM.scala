package kmeans

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.{LoggerFactory, Logger}
import spark.SparkObj


/**
  * Created by ad on 2016/1/23.
  */
object KM {
    val logger: Logger = LoggerFactory.getLogger(KM.getClass)

    def main(args: Array[String]) {
        val master = "hdfs://192.168.1.121:9000"
        val input = master + "/hzl/input/"
        val data = SparkObj.ctx.textFile(input)
        val parsedData = data.map(s => Vectors.dense(s.split(' ').map(_.toDouble))).cache()
        km(parsedData)
        SparkObj.ctx.stop()
    }

    def km(dataVec: RDD[Vector]) = {
        val acc = SparkObj.ctx.accumulator(0.0, "kmsees")
        for (i <- 2 until ConfigKM.max_clusters) {
            // Cluster the data into two classes using KMeans
            val clusters = KMeans.train(dataVec, i, ConfigKM.numIterations)
            // Evaluate clustering by computing Within Set Sum of Squared Errors
            val sse = clusters.computeCost(dataVec)
            acc.value_=(sse)
            logger.info("Within Set Sum of Squared Errors = " + acc.value)
            //            clusters
        }
    }
}
