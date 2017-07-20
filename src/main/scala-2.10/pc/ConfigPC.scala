package pc

/**
  * Created by hu on 2015/11/29.
  */
object ConfigPC {

  val k_max = 240 //最大的拟合线段数量

  val f = 1.5 //固定值

  val PI = 3.141592653 //π

  val iter_thresValue = math.exp(-19) //内部迭代停止

  val iter_thresTimes = 1150 //内部迭代停止

  var lamba = 0.5 //the angle-penalty weighter

  val Infs = 1.0e-10 //定义一个小数

  val Infb = Double.MinValue //定义一个小数

  var sigma = 0.06 //点横向方差

  def alpha = 2 * math.pow(sigma, 2) // 2*(d-1)*sigma^2 the smoothing parameter
}