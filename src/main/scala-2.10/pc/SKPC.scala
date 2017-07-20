package pc

import breeze.linalg._
import breeze.numerics.{exp, log}
import breeze.plot._
import org.slf4j.{Logger, LoggerFactory}
import util.{MatrixUtil, Orie, VectorUtil}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by hu on 2015/11/25.
  * 数据矩阵以列的方式存储(每一列即为一条数据)
  */
object SKPC {
  //设置日志
  val logger: Logger = LoggerFactory.getLogger(SKPC.getClass)

  def main(args: Array[String]) {

    val dataPath = "D:\\PC_SPARK\\data\\spirl" //s"D:\\PC_SPARK\\data\\SKEffectTest.csv" //args(0)//
    //测试数据
    val data: DenseMatrix[Double] = PCUtil.csv2Mat(dataPath, 2)
    //    val mv = MatrixUtil.matrixLess1(data)
    //    ConfigPC.sigma /= mv //动态调整参数
    //        val dataPath = "F:\\Tongji_PC\\data\\sp_5p.csv"
    //    0.1650662157040177,-0.2596417602636582,0.20081371814241156,0.5914742631565687,0.04115556316137581,-0.6193396274765216,0.21202240605967737,-0.12730671073967065
    //    0.1047926638651153,-0.1560649013216757,-0.6555679055923007,-0.1752710175170968,-0.7259388699343813,-0.46251566506329855,-0.01622364918743628,-0.25941136591256025
    var result = extractPC(data)
    println(MatrixUtil.matrix2String(result, "reduce"))
    result = PCUtil.filterOverfitLines(result, ConfigPC.sigma / 1.0)
    //        print(MatrixUtil.matrix2String(result))
    //    PCUtil.mat2csv("D:\\lines.csv", result)
    //        logger.info("the best num of lines is:" + ((result.cols / 4) + 1))
    logger.info("the best num of lines is:" + (result.cols / 2))
    logger.info("******complete successfully!!!******")

    plotJoinedLines(result, data)
  }

  def plotJoinedLines(lines: DenseMatrix[Double], data: DenseMatrix[Double]) = {
    val f = Figure()
    val p = f.subplot(0)
    p += plot(data(::, 0), data(::, 1), '.')
    for (i <- 0 until lines.cols / 2) {
      if (i % 2 == 0) {
        p += plot(lines(0, 2 * i to 2 * i + 1).t, lines(1, 2 * i to 2 * i + 1).t, '-'
          , "k")
      } else {
        p += plot(lines(0, 2 * i to 2 * i + 1).t, lines(1, 2 * i to 2 * i + 1).t, '-'
          , "r")
      }
    }

    p.title = "principal curves"
    p.xlabel = "x axis"
    p.ylabel = "y axis"
//    f.saveas("../data/lines.png")
  }

  //    val lines = (data: DenseMatrix[Double]) => {
  //        MatrixUtil.matrix2String(extractPC(data))
  //    }

  def extractPC(data: DenseMatrix[Double]) = {
    logger.info("******start compute principal curves...******")

    //求数据点两两之间的距离
    logger.info("compute distance of pairs of data...")
    val pairDis = MatrixUtil.matrixPairDS(data.t, data.t)
    //当前插入的线段条数
    var k = 0
    //数据的均值
    var cent = MatrixUtil.matrixMean(data, Orie.Vert)
    //特征值和特征向量
    var pca = MatrixUtil.matrixFirstPCA(data)
    var eigenVal = pca._1 //特征值
    var eigenVec = pca._2 //特征向量
    eigenVec :*= (ConfigPC.f * math.sqrt(eigenVal))
    //线段起点 过数据中心点
    var strPoLine = cent - eigenVec
    //线段终点
    var endPoLine = cent + eigenVec
    //矩阵每一列存储线段的一个端点
    logger.info("init the first line...")
    val lines = new DenseMatrix[Double](2, ConfigPC.k_max * 2)
    lines(::, 0) := strPoLine
    lines(::, 1) := endPoLine
    //求各个点到当前向量的投影距离
    var pro = PCUtil.distPro(strPoLine, endPoLine, data.t)
    var proDist = pro._1 //投影距離
    var proIndex = pro._2 //投影坐標 弧長表示
    var proPoint = pro._3 //投影点坐标
    logger.info("init project distance matrix...")
    val dists = DenseMatrix.zeros[Double](data.rows, ConfigPC.k_max)
    dists(::, k) := proDist
    //构造哈密顿路劲 一条线无需构造
    //将数据映射到隐变量
    var currLines = lines(::, 0 to 2 * k + 1)
    var linkedLines = PCUtil.linkLines(currLines)
    var acl = PCUtil.map2Acrl(currLines, linkedLines, data)
    var minValue = acl._1
    var y = acl._2
    var joinLines = acl._3
    val targetFunc = new ArrayBuffer[Double]()
    targetFunc.append(VectorUtil.vectorMean(minValue) +
      2 * ConfigPC.alpha * math.log(max(y(::, 0))))
    var sigma = VectorUtil.vectorMean(minValue)
    val mu = new ArrayBuffer[DenseVector[Double]]()
    mu.append(cent)
    val ratio = new ArrayBuffer[Double]()
    ratio.append(norm(endPoLine - strPoLine))
    var pl = DenseVector.zeros[Double](ConfigPC.k_max)

    pl(0) = 1.0
    var oldLogLike = ConfigPC.Infb
    logger.info("init completed,start compute...")
    logger.info("now it is the " + (k + 1) + " line...")
    var goon = true
    while (k < ConfigPC.k_max - 1 && goon) {
      val minProDist = MatrixUtil.matrixMin(dists(::, 0 to k), Orie.Horz)
      //      val ss=VectorUtil.vectorRep(minProDist, data.rows, Orie.Horz)
      var diff = VectorUtil.vectorRep(minProDist, data.rows, Orie.Horz) - pairDis
      diff = max(diff, 0.0)
      val moreThan1P = min(diff, ConfigPC.Infs)
      moreThan1P :/= ConfigPC.Infs
      var moreThan2P = MatrixUtil.matrixSum(moreThan1P, Orie.Vert)
      moreThan2P = max(moreThan2P, 2.0)
      moreThan2P = min(moreThan2P, 3.0)
      moreThan2P :-= 2.0
      val moreThan3P = moreThan2P :* MatrixUtil.matrixSum(diff, Orie.Vert)
      val argIndex = argmax(moreThan3P) //使得距离减小最大的点
      val adjPointIndex = diff(::, argIndex).findAll(_ > 0.0)
      if (adjPointIndex.length < 3) {
        logger.warn("the num of points not more than 2,stop!!")
        goon = false //计算完成 退出系统
      }
      if (goon) {
        logger.info("found an area of points to insert line...")
        k += 1
        val XS = MatrixUtil.getAdjustPoint(data, adjPointIndex)
        cent = MatrixUtil.matrixMean(XS, Orie.Vert)
        val diffXS = XS - VectorUtil.vectorRep(cent, XS.rows, Orie.Vert)
        pca = MatrixUtil.matrixFirstPCA(diffXS)
        eigenVal = pca._1
        eigenVec = pca._2
        eigenVec :*= (ConfigPC.f * math.sqrt(eigenVal))
        strPoLine = cent - eigenVec
        endPoLine = cent + eigenVec
        logger.info("new line is added to lines matrix...")
        logger.info("now it is the " + (k + 1) + " line...")
        lines(::, k * 2) := strPoLine
        lines(::, k * 2 + 1) := endPoLine
        mu.append(cent)
        pl :*= (k / (k + 1.0))
        pl(k) = 1.0 / (k + 1.0)
        ratio.append(2 * ConfigPC.f * math.sqrt(eigenVal))
        pro = PCUtil.distPro(strPoLine, endPoLine, data.t)
        proDist = pro._1 //投影距離
        dists(::, k) := proDist
        pro = PCUtil.distPro(strPoLine, endPoLine, XS.t)
        proDist = pro._1 //投影距離
        proIndex = pro._2 //投影坐標 弧長表示
        proPoint = pro._3 //投影点坐标
        sigma = k * sigma / (k + 1) + VectorUtil.vectorMean(proDist) / (2 * (k + 1))
        val temp_sig = DenseVector.zeros[Double](k + 1)
        var pix = DenseMatrix.zeros[Double](data.rows, k + 1)

        for (i <- 0 to k) {
          var tt = dists(::, i).copy
          tt :*= -1.0 / (2 * sigma)
          tt = exp(tt)
          tt :/= ratio(i) * 2.0 * ConfigPC.PI * sigma
          tt :*= pl(i)
          pix(::, i) := tt
        }

        var newLogLike = sum(log(sum(pix(*, ::))))
        if (newLogLike < oldLogLike) {
          logger.info("insert line not improve target function!!!")
        }
        var iterators = 0
        var change = 1.0
        logger.info("adjust all the lines...")
        while ((change > ConfigPC.iter_thresValue) && (iterators < ConfigPC.iter_thresTimes)) {
          print("*")
          pix = DenseMatrix.zeros[Double](data.rows, k + 1)
          for (i <- 0 to k) {
            var tt = dists(::, i).copy
            tt :*= -1.0 / (2 * sigma)
            tt = exp(tt)
            tt :/= ratio(i) * 2.0 * ConfigPC.PI * sigma
            tt :*= pl(i)
            pix(::, i) := tt
          }
          newLogLike = sum(log(sum(pix(*, ::))))
          val tt = VectorUtil.vectorRep(MatrixUtil.matrixSum(pix, Orie.Horz), pix.cols, Orie.Horz)
          pix = pix :/ tt
          for (i <- 0 to k) {
            val weights = pix(::, i) :/ sum(pix(::, i))
            val tmp = VectorUtil.vectorRep(weights, data.cols, Orie.Horz)
            mu(i) = MatrixUtil.matrixSum(data :* tmp, Orie.Vert)
            val td = data - VectorUtil.vectorRep(mu(i), data.rows, Orie.Vert)
            val wtd = td :* tmp
            pca = MatrixUtil.matrixFirstPCA(wtd)
            eigenVal = pca._1
            eigenVec = pca._2
            val ttp = td * eigenVec
            val adjOnPca = sum(ttp :* ttp :* weights) //加平方
            eigenVec :*= (ConfigPC.f * math.sqrt(adjOnPca) - sigma)
            strPoLine = mu(i) - eigenVec
            endPoLine = mu(i) + eigenVec
            lines(::, 2 * i) := strPoLine
            lines(::, 2 * i + 1) := endPoLine
            ratio(i) = 2 * ConfigPC.f * math.sqrt(adjOnPca)
            pro = PCUtil.distPro(strPoLine, endPoLine, data.t)
            proDist = pro._1 //投影距離
            dists(::, i) := proDist
            pl(i) = sum(pix(::, i)) / data.rows
            temp_sig(i) = sum(proDist :* pix(::, i))
          }
          sigma = sum(temp_sig) / (2 * data.rows)
          pl(0 to k) := max(pl(0 to k), 10.0 / data.rows)
          pl = pl :/ sum(pl)
          sigma = max(sigma, 0.0001)
          change = newLogLike - oldLogLike
          oldLogLike = newLogLike
          iterators += 1
        }
        println("afasdfsdf")
        logger.info("adjust completed,an new line may be added again...")
        //确定线的连接方式
        currLines = lines(::, 0 to 2 * k + 1)
        linkedLines = PCUtil.linkLines(currLines)
        acl = PCUtil.map2Acrl(currLines, linkedLines, data)
        minValue = acl._1
        y = acl._2
        targetFunc.append(VectorUtil.vectorMean(minValue) +
          2 * ConfigPC.alpha * math.log(max(y(::, 0))))
        goon = targetFunc(k) - targetFunc(k - 1) <= 0
        if (goon) {
          joinLines = acl._3
        } else {
          logger.warn("add the new line lead to overfit,stop afore!!")
        }
      }
    }
    joinLines
  }
}
