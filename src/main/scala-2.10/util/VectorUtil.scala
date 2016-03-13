package util

import breeze.linalg._
import pc.ConfigPC
import util.Orie.Orie

import scala.collection.mutable.ArrayBuffer

/**
  * Created by hu on 2015/11/29.
  */
object VectorUtil {
  /**
    * 向量复制
    *
    * @param v      向量
    * @param n      复制次数
    * @param direct 复制方向
    * @return 矩阵
    */
  def vectorRep(v: DenseVector[Double], n: Int, direct: Orie): DenseMatrix[Double] = {
    direct match {
      //可以通过赋值的方式实现，效率可能更高
      //测试完成进行实现，比较速度
      case Orie.Horz => {
        //20163-3 重构方法 估计能提高50倍性能
        val m = DenseMatrix.zeros[Double](v.length, n)
        for (i <- 0 until n) m(::, i) := v
        m

        //                val m = v.asDenseMatrix.t
        //                MatrixUtil.matrixRep(m, n, direct)

      }
      case Orie.Vert => {
        val m = DenseMatrix.zeros[Double](n, v.length)
        for (i <- 0 until n) m(i, ::) := v.t
        m
        //        val m = v.asDenseMatrix
        //        MatrixUtil.matrixRep(m, n, direct)
      }
    }
  }

  /**
    * 将向量乘以一个常数
    *
    * @param v     向量
    * @param scale 常数
    * @return 扩大后的向量(scala*x matlab)
    */
  def vectorScale(v: DenseVector[Double], scale: Double): DenseVector[Double] = {
    val t = v.copy
    t :*= scale
    t
  }

  def moreThanThres(v: DenseVector[Double], thres: Double): DenseVector[Double] = {
    moreThanThres(v, thres, thres)
  }

  /**
    * 所有数字必须大于等于给的的阈值
    * 否则，用一个其他数字代替
    *
    * @param v       向量
    * @param thres   阈值
    * @param replace 代替值
    * @return
    */
  def moreThanThres(v: DenseVector[Double], thres: Double, replace: Double): DenseVector[Double] = {
    v.map(e => {
      if (e < thres) replace else e
    })
  }

  def lessThanThres(v: DenseVector[Double], thres: Double): DenseVector[Double] = {
    lessThanThres(v, thres, thres)
  }

  /**
    * 设置所有数字必须小于等于一个阈值
    * 如果大于用指定数字代替
    *
    * @param v
    * @param thres
    * @param replace
    * @return
    */
  def lessThanThres(v: DenseVector[Double], thres: Double, replace: Double): DenseVector[Double] = {
    v.map(e => {
      if (e > thres) replace else e
    })
  }

  /**
    * 向量进行归一化
    *
    * @param v 向量
    * @return 归一化向量
    */
  def vectorNorm(v: DenseVector[Double]): DenseVector[Double] = {
    val tv = v.copy
    val mm = minMax(tv)
    tv -= (mm._1)
    tv *= (1.0 / (mm._2 - mm._1))
    tv
  }

  /**
    * 求两个向量的距离
    *
    * @param v1 向量v1
    * @param v2 向量v2
    * @return 距离
    */
  def edPowDistance(v1: DenseVector[Double], v2: DenseVector[Double]) = {
    val diff = v1 - v2
    sum(diff :* diff)
  }

  /**
    * 求两个向量之间的夹角
    *
    * @param v1 向量v1
    * @param v2 向量v2
    * @return 角度的弧度表示
    */
  def arc(v1: DenseVector[Double], v2: DenseVector[Double]) = {
    val tv1 = v1.copy
    val tv2 = v2.copy
    tv1 :/= norm(tv1)
    tv2 :/= norm(tv2)
    math.acos(tv1.t * tv2) / ConfigPC.PI
  }

  /**
    * 大于某一个数字的索引集合
    *
    * @param v
    * @param thres
    */
  def moreThanThresIndeces(v: DenseVector[Double], thres: Double) = {
    val arr = new ArrayBuffer[Int]()
    v.foreachPair((key, value) => {
      if (value > thres) arr.append(key)
    })
    arr
  }

  /**
    * 向量的均值
    *
    * @param v
    * @return
    */
  def vectorMean(v: DenseVector[Double]) = {
    sum(v) / v.length
  }

  /**
    * 将向量转成字符串，用两个
    * 空格隔开
    * @param v
    * @return
    */
  def vector2String(v: DenseVector[Int]) = {

    "[" + v.toArray.mkString(",") + "]"
  }

}
