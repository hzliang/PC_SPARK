package spark

import org.apache.spark.AccumulatorParam

/**
  * Created by hy on 16-1-25.
  */
object StringAccumulableParam extends AccumulatorParam[String] {

  val separator = "\n"

  override def addAccumulator(r: String, t: String): String = {
    if (r.isEmpty()) t
    else if (t.isEmpty()) r
    else r + separator + t
  }

  override def addInPlace(r: String, t: String): String = {
    if (r.isEmpty()) t
    else if (t.isEmpty()) r
    else r + separator + t
  }

  def zero(initialValue: String): String = {
    ""
  }
}
