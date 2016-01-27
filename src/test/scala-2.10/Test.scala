import breeze.linalg.{DenseVector, DenseMatrix}
import util.MatrixUtil

/**
  * Created by ad on 2015/12/27.
  */
object Test {
  def main(args: Array[String]) {
    //    var oldLink = new Array[Int](2)
    //    oldLink = oldLink.map(_ + 1)
    //    println(oldLink.mkString(","))
    //    val LinkD = DenseVector[Int](2, 3, 4, 5, 1, 9)
    //    val re = PCUtil.link(oldLink, LinkD)
    //    val m1 = DenseMatrix.zeros[Double](2, 2)
    //    val m2 = DenseMatrix.zeros[Double](2, 2)
    //    print(MatrixUtil.matrix2String(m2, "matlab"))
    val m = "1,2\n"
    print(MatrixUtil.matrixStr2Matrix(m))
  }
}
