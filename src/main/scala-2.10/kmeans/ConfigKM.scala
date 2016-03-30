package kmeans

/**
  * Created by ad on 2016/1/23.
  */
object ConfigKM {

  var totalDataCount: Long = 300

  var classDataNum: Int = 3500

  val itersTimes: Int = 20 //聚类迭代次数

  val reRunTimes: Int = 1 //重复运行次数。选择最优的

  def classCount: Int = Math.ceil(totalDataCount / classDataNum).toInt //类别数量

}
