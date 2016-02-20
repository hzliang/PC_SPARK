package spark

import org.apache.spark.AccumulatorParam
import org.apache.spark.mllib.linalg.{Vectors, Vector}

/**
  * Created by hy on 16-2-20.
  */
object ClusterAccumulableParam extends AccumulatorParam[Array[(Int, Vector)]] {

  override def addAccumulator(r: Array[(Int, Vector)], t: Array[(Int, Vector)]): Array[(Int, Vector)] = {
    if (r.isEmpty) t
    else if (t.isEmpty) r
    else r.union(t)
  }

  override def addInPlace(r: Array[(Int, Vector)], t: Array[(Int, Vector)]): Array[(Int, Vector)] = {
    if (r.isEmpty) t
    else if (t.isEmpty) r
    else r.union(t)
  }

  def zero(initialValue: Array[Tuple2[Int, Vector]]): Array[Tuple2[Int, Vector]] = {
    new Array[(Int, Vector)](0)
  }
}
