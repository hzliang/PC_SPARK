package run

import breeze.linalg.{DenseVector, DenseMatrix}
import kmeans.KM
import org.apache.spark.mllib.linalg.Vectors
import pc.SKPC
import spark.SparkObj

/**
  * Created by ad on 2016/1/23.
  */
object Run {

    def main(args: Array[String]) {
        val master = "hdfs://192.168.1.121:9000"
        val input = master + "/hzl/input/cros3.csv" //输入文件
        //将输入每一行数据转成向量Dense(x,y)
        val dataRDD = SparkObj.ctx.textFile(input).map(s => Vectors.dense(s.split(',').map(_.toDouble))).cache()
        val clus = KM.km(dataRDD)
        clus.predict(dataRDD).zip(dataRDD).groupByKey.map(v => {
            val gi = v._2.toArray
            val data = DenseMatrix.zeros[Double](gi.length, 2)
            for (i <- 0 until gi.length) {
                data(i, ::) := DenseVector[Double](gi(i).toArray).t
            }
            SKPC.extractPC(data)
        })
    }
}
