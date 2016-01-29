import breeze.linalg.{Axis, DenseVector, DenseMatrix}
import breeze.plot._
import pc.PCUtil
import util.MatrixUtil

/**
  * Created by ad on 2015/12/27.
  */
object Test {
  def main(args: Array[String]) {
    testPllFun
    val v = DenseVector[Double](2, 2)
    println(v :/ 2.0)
  }

  def testPllFun() = {
    val m = DenseMatrix.zeros[Double](2, 6)
    m(::, 0) := DenseVector[Double](1.88, 1.0)
    m(::, 1) := DenseVector[Double](2.0, 2.0)
    m(::, 2) := DenseVector[Double](3.5, 3.5)
    m(::, 3) := DenseVector[Double](6.8, 4.8)
    m(::, 4) := DenseVector[Double](1.31, 1.51)
    m(::, 5) := DenseVector[Double](3.81, 3.81)

    val f = Figure()
    val p = f.subplot(0)
    p += plot(m(0, 0 to 1).t, m(1, 0 to 1).t, '-', "k")
    for (i <- 1 until m.cols / 2) {
      p += plot(m(0, 2 * i to 2 * i + 1).t, m(1, 2 * i to 2 * i + 1).t, '-', "r")
    }

    val res = PCUtil.filterPllLines(m, 10.0)
    p += plot(res(0, 0 to 1).t, res(1, 0 to 1).t, '-', "g")
  }


  def testIf() = {
    val m = DenseMatrix.zeros[Double](2, 4)
    m(::, 0) := DenseVector[Double](0.0, 0.0)
    m(::, 1) := DenseVector[Double](1.0, 1.0)
    m(::, 2) := DenseVector[Double](0.5, 0.5)
    m(::, 3) := DenseVector[Double](0.8, 0.8)

    println(m.delete(1, axis = Axis._1))
  }


}
