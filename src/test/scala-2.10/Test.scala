import breeze.linalg.{argmin, Axis, DenseVector, DenseMatrix}
import breeze.plot._
import pc.PCUtil
import util.MatrixUtil

/**
  * Created by ad on 2015/12/27.
  */
object Test {
  def main(args: Array[String]) {
    linkLines
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

    val res = PCUtil.filterOverfitLines(m, 10.0)
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

  def linkLines() = {
    val v1 = DenseVector(Array(0.243654363454576, -0.441951358669328,
      -0.294103232061543, -0.825241974674782,
      -0.869990471261836, -0.515558855295775,
      0.705342596282296, 0.906444732208413, 0.797270112380177,
      0.348250660470640, -0.0649050888695531, -0.500465360952894,
      -0.243729743618869, 0.118911482751592, 0.0524633833658770,
      0.356040105793606, 0.395143186654297, 0.166245877014115,
      0.199284518942376, 0.636170034626327, -0.344000000000000, -0.595000000000000))
    val v2 = DenseVector(Array(
      2.73176013554531, 2.37968599734750, 1.10422614921753, 1.61318815755853,
      1.81956133554417, 2.29812056835621, 2.17715141906553, 2.63533165738855,
      2.76696541655028, 2.74301284251764, 0.649696142574734, 0.353916854861278,
      0.992356663956000, 1.39174540147898, 0.762046246283292, 1.35116232764329,
      1.51811151539114, 2.12747387175960, 1.53040378668793, 2.06847743612327,
      0.859400000000000, 0.400000000000000))
    val m = new DenseMatrix[Double](2, 22)
    m(0, ::) := v1.t
    m(1, ::) := v2.t
    println(PCUtil.linkLines(m))

  }

  def zipTest() = {
    val a = new Array[Int](2)
    a(0) = 0
    a(1) = 1

    val b = new Array[Int](2)
    b(0) = 0
    b(1) = 1
    val c = a zip b
    c.foreach(println)
  }

}
