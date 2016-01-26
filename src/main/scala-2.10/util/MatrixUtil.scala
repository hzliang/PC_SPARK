package util

import breeze.linalg._
import breeze.numerics.abs
import breeze.stats.mean
import util.Orie.Orie

/**
  * Created by hu on 2015/11/25.
  */
object MatrixUtil {

  /**
    * 求矩阵两两点之间的距离
    *
    * @param m1 矩阵a m*n
    * @param m2 矩阵b m*n
    * @return 矩阵 m*m
    */
  def matrixPairDS(m1: DenseMatrix[Double], m2: DenseMatrix[Double]): DenseMatrix[Double] = {
    val aa = sum(m1 :* m1, Axis._0)
    val bb = sum(m2 :* m2, Axis._0)
    val ab: DenseMatrix[Double] = m1.t * m2
    ab :*= 2.0
    abs(matrixRep(aa.t, bb.cols, Orie.Horz) + matrixRep(bb, aa.cols, Orie.Vert) - ab)
  }

  /**
    * 对矩阵进行复制
    *
    * @param m      矩阵
    * @param n      复制次数
    * @param direct 复制方向
    * @return 复制后的矩阵
    */
  def matrixRep(m: DenseMatrix[Double], n: Int, direct: Orie): DenseMatrix[Double] = {
    def repmat(t: DenseMatrix[Double], k: Int): DenseMatrix[Double] = {
      if (k <= 1) t
      else {
        val y = direct match {
          case Orie.Vert => DenseMatrix.vertcat(t, m)
          case Orie.Horz => DenseMatrix.horzcat(t, m)
          case _ => DenseMatrix.vertcat(t, m)
        }
        repmat(y, k - 1)
      }
    }
    repmat(m, n)
  }

  /**
    * 求矩阵均值
    *
    * @param m 矩阵
    * @return 向量(均值)
    */
  def matrixMean(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => mean(m(*, ::)).toDenseVector //水平方向
      case Orie.Vert => mean(m(::, *)).toDenseVector //竖直方向
    }
  }

  /**
    * 求矩阵的行列和
    *
    * @param m
    * @param direct
    * @return
    */
  def matrixSum(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => sum(m(*, ::)).toDenseVector //水平方向
      case Orie.Vert => sum(m(::, *)).toDenseVector //竖直方向
    }
  }

  /**
    * 求矩阵第一主成分(eig(cov(x)))
    *
    * @param m 矩阵
    * @return _1 特征值 _2 特征向量
    */
  def matrixFirstPCA(m: DenseMatrix[Double]): Tuple2[Double, DenseVector[Double]] = {
    val eigen = eig(matrixCov(m))
    val eigValues = eigen.eigenvalues
    val maxIndex = argmax(eigValues)
    val eigenvector = eigen.eigenvectors(::, maxIndex)
    eigenvector :*= -1.0
    (eigValues(maxIndex), eigenvector)
  }

  /**
    * 矩阵的协方差矩阵
    *
    * @param m 矩阵
    * @return 协方差矩阵
    */
  def matrixCov(m: DenseMatrix[Double]): DenseMatrix[Double] = {
    var zeMat = zeroMean(m)
    zeMat = zeMat.t * zeMat
    zeMat :*= (1.0 / (m.rows - 1))
    zeMat
  }

  /**
    * 将矩阵均值归零
    *
    * @param m 矩阵
    * @return 均值归零
    */
  def zeroMean(m: DenseMatrix[Double]): DenseMatrix[Double] = {
    val copy = m.copy
    for (c <- 0 until m.cols) {
      val col = copy(::, c)
      val colMean = mean(col)
      col -= colMean
    }
    copy
  }

  /**
    * 将矩阵转成字符串
    *
    * @param m 矩阵
    * @return 字符串
    */
  def matrix2String(m: DenseMatrix[Double], optType: String = "matlab"): String = {
    optType.toLowerCase match {
      case "matlab" => {
        val str = new StringBuilder("[")
        for (i <- 0 until m.rows) {
          for (j <- 0 until m.cols) {
            str.append(m(i, j) + ",")
          }
          str.replace(str.length - 1, str.length, ";\n")
        }
        str.replace(str.length - 2, str.length, "]").toString
      }
      case "reduce" => {
        val str = new StringBuilder()
        for (i <- 0 until m.rows) {
          for (j <- 0 until m.cols) {
            str.append(m(i, j) + ",")
          }
          str.replace(str.length - 1, str.length, "\n")
        }
        str.toString
      }
      case _ => ""
    }

  }

  /**
    * 将两个字符串形式的矩阵相连
    *
    * @param m1
    * @param m2
    */
  def matrixPlus2String(m1: String, m2: String) = {

    val m1Str = m1.split("\n")
    val m2Str = m2.split("\n")
    val sb = new StringBuilder()
    sb.append(m1Str(0) + "," + m2Str(0) + "\n")
    sb.append(m1Str(1) + "," + m2Str(1))
    sb.toString
  }

  /**
    * 数据矩阵归一化
    * 按列归一化
    *
    * @param m
    * @return
    */
  def matrixNorm(m: DenseMatrix[Double]) = {
    for (i <- 0 until m.cols) {
      m(::, i) := VectorUtil.vectorNorm(m(::, i))
    }
  }

  /**
    * 将所有数据进行一个倍数放缩
    * 使数据在-1~+1之间
    *
    * @param m
    * @return
    */
  def matrixLess1(m: DenseMatrix[Double]) = {
    val mv = max(abs(m))
    m :/= mv
    mv
  }

  /**
    * 通过索引的向量坐标获取矩阵
    *
    * @param m
    * @param v
    * @return
    */
  def getAdjustPoint(m: DenseMatrix[Double], v: IndexedSeq[Int]) = {
    val tm = DenseMatrix.zeros[Double](v.length, m.cols)
    var k = 0
    for (idx <- v) {
      tm(k, ::) := m(idx, ::)
      k += 1
    }
    tm
  }

  /**
    * 沿着某一个方向求矩阵的最小值
    *
    * @param m
    * @param direct
    * @return
    */
  def matrixMin(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => min(m(*, ::)).toDenseVector
      case Orie.Vert => min(m(::, *)).toDenseVector
    }
  }

  /**
    * 沿着某一个方向求矩阵的最小值
    *
    * @param m
    * @param direct
    * @return
    */
  def argMatrixMin(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => argmin(m(*, ::)).toDenseVector
      case Orie.Vert => argmin(m(::, *)).toDenseVector
    }
  }

  /**
    * 最小值与对应坐标
    *
    * @param m
    * @param direct
    * @return
    */
  def mmMatrixMin(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => (min(m(*, ::)).toDenseVector, argmin(m(*, ::)).toDenseVector)
      case Orie.Vert => (min(m(::, *)).toDenseVector, argmin(m(::, *)).toDenseVector)
    }
  }

  /**
    * 沿着某一个方向求矩阵的最大值
    *
    * @param m
    * @param direct
    * @return
    */
  def matrixMax(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => max(m(*, ::)).toDenseVector
      case Orie.Vert => max(m(::, *)).toDenseVector
    }
  }

  /**
    * 沿着某一个方向求矩阵的最大值
    *
    * @param m
    * @param direct
    * @return
    */
  def argMatrixMax(m: DenseMatrix[Double], direct: Orie) = {
    direct match {
      case Orie.Horz => argmax(m(*, ::)).toDenseVector
      case Orie.Vert => argmax(m(::, *)).toDenseVector
    }
  }
}
