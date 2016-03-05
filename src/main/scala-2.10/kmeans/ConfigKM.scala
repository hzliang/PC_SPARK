package kmeans

/**
  * Created by ad on 2016/1/23.
  */
object ConfigKM {

  var totalDataCount: Long = 300

  val classDataNum: Int = 1000

  val itersTimes: Int = 20 //聚类迭代次数

  val reRunTimes: Int = 1 //重复运行次数。选择最优的

  def classCount: Int = (totalDataCount / classDataNum + 1).toInt //类别数量

}
