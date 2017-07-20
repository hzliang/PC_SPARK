package run

import java.net.URI

import breeze.linalg.{DenseMatrix, DenseVector}
import kmeans.{ConfigKM, KM}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD
import pc.{ConfigPC, PCUtil, SKPC}
import spark.SparkObj
import util.{MatrixUtil, VectorUtil}
import java.io.FileWriter
import java.io.File
import scala.collection.mutable.ArrayBuffer

/**
  * Created by ad on 2016/1/23.
  */
object Run {

  def main(args: Array[String]) {
    val master = "hdfs://127.0.0.1:9000"
    val input = "/root/PC_SPARK/data/SKEffectTest.csv" //输入文件
    //将输入每一行数据转成向量Dense(x,y)
    val dataRDD = SparkObj.ctx.textFile(input).map(s => {
        Vectors.dense(s.split('\t').map(_.toDouble))
      }).cache()
    //    val clusRDD = KM.kmeans(dataRDD).predict(dataRDD).zip(dataRDD).persist()
    ConfigKM.totalDataCount = dataRDD.count()
    //    val clusInfo = KMeans.train(parsedData, ConfigKM.classCount,
    //      ConfigKM.itersTimes, ConfigKM.reRunTimes).predict(parsedData).
    //      map((_, 1)).reduceByKey(_ + _)
//    ConfigKM.classDataNum = args(2).toInt
//    ConfigPC.sigma = args(3).toDouble
//    ConfigPC.lamba = args(4).toDouble
    //    val accum = SparkObj.ctx.accumulator(ConfigKM.classCount, "My Accumulator")
    val rddArray = new ArrayBuffer[RDD[(Int, Vector)]]()
    KM.kmeans(dataRDD, ConfigKM.classCount, ConfigKM.classCount, rddArray, false)
    val clusInfo = rddArray.reduce(_ union _)
    val output =master+ "/hzl/output/cluster"
    val hdfs = FileSystem.get(new URI(master), new Configuration())
    val outputFS = new Path(output)
    // 删除输出目录
    if (hdfs.exists(outputFS)) hdfs.delete(outputFS, true)
    clusInfo.repartition(1).saveAsTextFile(output)
    //    clusInfo.saveAsTextFile(output)
    //save hdfs

    //    val acc = SparkObj.ctx.accumulator("", "stringAcc")(StringAccumulableParam)
    //    clusRDD.foreach(v => acc += v._1 + "," + v._2.toArray.mkString(","))
    val allLines = clusInfo.groupByKey.map(v => {

      val data = v._2.toArray
      val dataMat = DenseMatrix.zeros[Double](data.length, 2)
      for (i <- 0 until data.length) {
        dataMat(i, ::) := DenseVector[Double](data(i).toArray).t
      }
      //转成字符串返回，带固定结果
      //shffle会需大量时间
      MatrixUtil.matrix2String(SKPC.extractPC(dataMat), "reduce")
      //      SKPC.extractPC(dataMat)
    }).reduce(MatrixUtil.matrixPlus2String(_, _))
    //        输出结果
    val sb = new StringBuilder
    sb.append(allLines+"\n")
    sb.append(VectorUtil.vector2String(
      PCUtil.linkLines(MatrixUtil.matrixStr2Matrix(allLines))))
    //    allLines.saveAsTextFile(output)
    val fw = new FileWriter(new File("/root/PC_SPARK/out"))
    fw.write(sb.toString())
    fw.close()
  }
}
