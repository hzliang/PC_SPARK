package run

import kmeans.KM
import org.apache.spark.mllib.linalg.Vectors
import spark.SparkObj

/**
  * Created by ad on 2016/1/23.
  */
object Run {

    def main(args: Array[String]) {
        val master = "hdfs://192.168.1.121:9000"
        val input = master + "/hzl/input/cros3.csv"
        val data = SparkObj.ctx.textFile(input).map(s => Vectors.dense(s.split(',').map(_.toDouble))).cache()
        val clus = KM.km(data)
        val clusterRes = clus.predict(data).fo
    }
}
