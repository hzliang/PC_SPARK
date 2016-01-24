package kmeans

import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.rdd.RDD
import org.slf4j.{Logger, LoggerFactory}
import spark.SparkObj


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
        val clus = km(parsedData)
        val resRDD = clus.predict(parsedData).zip(parsedData)
        val output = master + args(1) //"/hzl/output/cluster"
        resRDD.saveAsTextFile(output)
        SparkObj.ctx.stop()
    }

    def km(data: RDD[Vector]) = {
        //聚类
        logger.info("set the num of classes is 2, start computer...")
        var clu = KMeans.train(data, 3, ConfigKM.numIter, ConfigKM.runs)
        //代价
        //        var sse = clu.computeCost(data)
        //        logger.info("compute sse,the target function cost is:" + sse)
        //        val acc = SparkObj.ctx.accumulator(sse, "kmsees")
        //        var goon = true
        //        for (i <- 3 until ConfigKM.max_clusters if goon) {
        //            //聚类
        //            logger.info("set the num of classes is " + i + ", start computer...")
        //            clu = KMeans.train(data, i, ConfigKM.numIter, ConfigKM.runs)
        //            //代价
        //            sse = clu.computeCost(data)
        //            logger.info("compute sse,the target function cost is:" + sse)
        //            if (math.abs(acc.value - sse) <= 1) {
        //                logger.info("compute over,the best num of classes is:" + i)
        //                goon = false
        //            }
        //            //            println("Within Set Sum of Squared Errors = " + acc.value)
        //            //            clusters
        //        }
        clu
    }

    def bestClus(): Unit = {

    }
}
