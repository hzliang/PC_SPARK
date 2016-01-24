import breeze.linalg.DenseMatrix

/**
  * Created by ad on 2015/12/27.
  */
object Test {
  def main(args: Array[String]) {
    //    val oldLink = DenseVector[Int](8, 6, 2, 3, 4, 5)
    //    val LinkD = DenseVector[Int](2, 3, 4, 5, 1, 9)
    //    val re = PCUtil.link(oldLink, LinkD)
    val m1 = DenseMatrix.zeros[Double](2,2)
    val m2 = DenseMatrix.zeros[Double](2,2)
    println(DenseMatrix.horzcat(m1,m2))
  }
}
