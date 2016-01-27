package run

import breeze.linalg.{DenseMatrix, DenseVector}
import kmeans.KM
import org.apache.spark.mllib.linalg.Vectors
import pc.{PCUtil, SKPC}
import spark.{StringAccumulableParam, SparkObj}
import util.{MatrixUtil, VectorUtil}

/**
  * Created by ad on 2016/1/23.
  */
object Run {

  def main(args: Array[String]) {
    val master = "hdfs://192.168.1.121:9000"
    val input = master + args(0) //"/hzl/input/cros3.csv" //输入文件
    //将输入每一行数据转成向量Dense(x,y)
    val dataRDD = SparkObj.ctx.textFile(input).map(s => {
        Vectors.dense(s.split(',').map(_.toDouble))
      }).cache()
    val clusRDD = KM.km(dataRDD).predict(dataRDD).zip(dataRDD).persist()

    val acc = SparkObj.ctx.accumulator("", "stringAcc")(StringAccumulableParam)
    clusRDD.foreach(v => acc += v._1 + "," + v._2.toArray.mkString(","))
    val allLines = clusRDD.groupByKey.map(v => {
      val data = v._2.toArray
      val dataMat = DenseMatrix.zeros[Double](data.length, 2)
      for (i <- 0 until data.length) {
        dataMat(i, ::) := new DenseVector[Double](data(i).toArray).t
      }
      //转成字符串返回，带固定结果
      //shffle会需大量时间
      MatrixUtil.matrix2String(SKPC.extractPC(dataMat), "reduce")
    }).reduce(MatrixUtil.matrixPlus2String(_, _))
    //输出结果
    println("the cluster result is:")
    println(acc.value)
    println("The original lines are:")
    println(allLines)
    println("The linked lines are:")
    println(VectorUtil.vector2String(
      PCUtil.linkLines(MatrixUtil.matrixStr2Matrix(allLines))))
  }
}
