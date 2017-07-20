package kmeans

/**
  * Created by ad on 2016/1/23.
  */
object ConfigKM {

  var totalDataCount: Long = 300

  var classDataNum: Int = 300

  val itersTimes: Int = 20 //聚类迭代次数

  val reRunTimes: Int = 1 //重复运行次数。选择最优的

  //重复运行次数。选择最优的
  //类别数量
  def classCount: Int = {
    val count = Math.ceil(totalDataCount / classDataNum).toInt
    if (count == 0) 1 else count
  }

}
