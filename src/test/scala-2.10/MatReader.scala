import spire.std.float

/**
  * Created by root on 17-7-20.
  */
object MatReader {
  def main(args: Array[String]): Unit = {
    import com.jmatio.io.MatFileReader
    import com.jmatio.types.MLArray
    import com.jmatio.types.MLDouble
    val read = new MatFileReader("/root/PC_SPARK/data/spiral.mat")
    val mlArray = read.getMLArray("spiral") //mat存储的就是img矩阵变量的内容

    val d = mlArray.asInstanceOf[MLDouble]
    val matrix = d.getArray //只有jmatio v0.2版本中才有d.getArray方法


    matrix.foreach(i=>println(i(0).toString.substring(0,1)+"\t"+i(1).toString.substring(0,8)+"\t"+i(2).toString.substring(0,8)))
  }

}
