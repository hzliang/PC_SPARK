package run

import java.net.URI

import breeze.linalg.{DenseMatrix, DenseVector}
import kmeans.{ConfigKM, KM}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import pc.{PCUtil, SKPC}
import spark.{StringAccumulableParam, SparkObj}
import util.{MatrixUtil, VectorUtil}

import scala.collection.mutable.ArrayBuffer

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
    //    val clusRDD = KM.kmeans(dataRDD).predict(dataRDD).zip(dataRDD).persist()
    ConfigKM.totalDataCount = dataRDD.count()
    //    val clus = KMeans.train(parsedData, ConfigKM.classCount,
    //      ConfigKM.itersTimes, ConfigKM.reRunTimes).predict(parsedData).
    //      map((_, 1)).reduceByKey(_ + _)
    val accum = SparkObj.ctx.accumulator(ConfigKM.classCount, "My Accumulator")
    val rddArray = new ArrayBuffer[RDD[(Int, Vector)]]()
    KM.kmeans(dataRDD, ConfigKM.classCount, accum, accum.value, rddArray, false)
    val clus = rddArray.reduce(_ union _)

    val output = master + args(1) //"/hzl/output/cluster"
    val hdfs = FileSystem.get(new URI(master), new Configuration())
    val outputFS = new Path(output)
    // 删除输出目录
    if (hdfs.exists(outputFS)) hdfs.delete(outputFS, true)
    //    clus.repartition(1).saveAsTextFile(output)
    //    clus.saveAsTextFile(output)
    //save hdfs

    //    val acc = SparkObj.ctx.accumulator("", "stringAcc")(StringAccumulableParam)
    //    clusRDD.foreach(v => acc += v._1 + "," + v._2.toArray.mkString(","))
    val allLines = clus.groupByKey.map(v => {
      val data = v._2.toArray
      val dataMat = DenseMatrix.zeros[Double](data.length, 2)
      for (i <- 0 until data.length) {
        dataMat(i, ::) := new DenseVector[Double](data(i).toArray).t
      }
      //转成字符串返回，带固定结果
      //shffle会需大量时间
      MatrixUtil.matrix2String(SKPC.extractPC(dataMat), "reduce")
    })
    //      .reduce(MatrixUtil.matrixPlus2String(_, _))
    //    //输出结果
    //    //    println("the cluster result is:")
    //    //    println(acc.value)
    //    println("The original lines are:")
    //    println(allLines)
    //    println("The linked lines are:")
    //    println(VectorUtil.vector2String(
    //      PCUtil.linkLines(MatrixUtil.matrixStr2Matrix(allLines))))
    allLines.saveAsTextFile(output)
  }
}
