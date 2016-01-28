package spark

import breeze.linalg.{norm, DenseVector}

/**
  * Created by hy on 16-1-27.
  */
case class Line(start: DenseVector[Double], end: DenseVector[Double]) extends Ordered[Line] {

  override def toString = norm(start - end).toString

  override def hashCode = {
    (start.hashCode + end.hashCode) % Int.MaxValue
  }

  override def equals(o: Any) = o match {
    case that: Line => start.equals(that.start) && end.equals(that.end)
    case _ => false
  }

  override def compare(that: Line): Int = {
    val diff = norm(start - end) - norm(that.start - that.end)
    if (diff > 0) 1 else if (diff < 0) -1 else 0
  }

//  def unapply(start: DenseVector[Double], end: DenseVector[Double]) = DenseVector.horzcat(start, end)
}
