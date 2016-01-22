import breeze.linalg._
import util.MatrixUtil

/**
  * Created by ad on 2016/1/16.
  */
object VizTest {
  def main(args: Array[String]) {
    val m = DenseMatrix.rand[Double](2, 2)
    println(MatrixUtil.matrix2String(m))

  }
}
