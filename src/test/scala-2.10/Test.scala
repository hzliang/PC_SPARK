import breeze.linalg.{Axis, DenseVector, DenseMatrix}
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
    m(::, 0) := DenseVector[Double](1.0, 1.0)
    m(::, 1) := DenseVector[Double](2.0, 2.0)
    m(::, 2) := DenseVector[Double](0.5, 0.5)
    m(::, 3) := DenseVector[Double](0.8, 0.8)
    m(::, 4) := DenseVector[Double](100.51, 100.51)
    m(::, 5) := DenseVector[Double](100.81, 100.81)


    val res = PCUtil.filterPllLines(m, 10.0)
    println(res)

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
