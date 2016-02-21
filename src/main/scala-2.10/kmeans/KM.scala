package kmeans

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
    ConfigKM.totalDataCount = parsedData.count()
    val clus = kmeans(parsedData)
    //        val resRDD = clus.predict(parsedData).zip(parsedData)
    //        val output = master + args(1) //"/hzl/output/cluster"
    //        resRDD.saveAsTextFile(output)
    SparkObj.ctx.stop()
  }

  def kmeans(data: RDD[Vector]) = {
    //聚类
    logger.info("set the num of classes is " + ConfigKM.classCount + ", start computer...")
    val accum = SparkObj.ctx.accumulator(ConfigKM.classCount, "clusterNum")
    val clus = KMeans.train(data, ConfigKM.classCount, ConfigKM.itersTimes, ConfigKM.reRunTimes)
    val resRDD = clus.predict(data).zip(data)
    //    resRDD.groupByKey.mapValues(v => {
    //      val currClassData = v.toArray
    //      val currClassDataLen = currClassData.length
    //      if (currClassDataLen >= ConfigKM.classDataNum * 1.5) {
    //        val clusNumTmp = currClassData.length / ConfigKM.classDataNum + 1
    //        val tmpRDD = SparkObj.ctx.makeRDD(currClassData).persist()
    //        val clusTmp = KMeans.train(tmpRDD, clusNumTmp, ConfigKM.itersTimes, ConfigKM.reRunTimes)
    //        val currNum = accum.value
    //        accum += clusNumTmp
    //        clusTmp.predict(tmpRDD).map(tv => {
    //          tv + currNum
    //        }).zip(data)
    //      }
    //      else {
    //        v
    //      }
    //    })

    resRDD.partitionBy(new ClusterPartitioner(ConfigKM.classCount)).mapPartitions(v => {
      val currClassData = for (elem <- v) yield elem._2
      val currClassDataLen = currClassData.length
      if (currClassDataLen >= ConfigKM.classDataNum * 1.5) {
        val clusNumTmp = currClassData.length / ConfigKM.classDataNum + 1
        val tmpRDD = SparkObj.ctx.makeRDD(currClassData.toIndexedSeq).persist()
        val clusTmp = KMeans.train(tmpRDD, clusNumTmp, ConfigKM.itersTimes, ConfigKM.reRunTimes)
        val currNum = accum.value
        accum += clusNumTmp
        clusTmp.predict(tmpRDD).map(tv => {
          tv + currNum
        }).zip(data).collect().toIterator
      }
      else {
        v
      }
    })
  }

}
