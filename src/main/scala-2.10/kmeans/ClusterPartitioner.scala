package kmeans

import org.apache.spark.Partitioner

/**
  * Created by huzuo on 2016/2/21.
  */
class ClusterPartitioner(numParts: Int) extends Partitioner {
  override def numPartitions: Int = numParts

  override def getPartition(key: Any): Int = {
    key match {
      case (id, data) => id.toString.toInt
      case _ => 0
    }
  }

  override def equals(other: Any): Boolean = other match {
    case clust: ClusterPartitioner => clust.numPartitions == numPartitions
    case _ => false
  }

  override def hashCode: Int = numPartitions
}