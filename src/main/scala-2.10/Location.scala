/**
  * Created by ad on 2016/1/5.
  */
class Location(val lo: Double, val la: Double) extends Ordering[Location] {
  override def toString = lo + "," + la

  override def hashCode = lo.hashCode + la.hashCode

  override def compare(x: Location, y: Location): Int = {
    val isEqu = x.equals(y)
    if (isEqu) {
      0
    } else {
      if (x.lo == y.lo) {
        if (x.la >= y.la) 1 else -1
      } else {
        if (x.lo >= y.lo) 1 else -1
      }
    }
  }

  override def equals(o: Any) = o match {
    case that: Location => this.lo == that.lo && this.la == that.la
    case _ => false
  }
}
