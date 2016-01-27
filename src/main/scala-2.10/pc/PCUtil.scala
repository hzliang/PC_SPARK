package pc

import java.io.{BufferedWriter, File, FileWriter}

import breeze.linalg._
import util.{MatrixUtil, Orie, VectorUtil}

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by hu on 2015/12/3.
  */
object PCUtil {
  /**
    * 计算数据点到一组线的距离
    *
    * @param bathLines 数组：矩阵存储这组线
    * @param data      数据
    * @return 距离 投影长度 投影点
    */
  def bathDistPro(bathLines: DenseMatrix[Double], data: DenseMatrix[Double])
  : IndexedSeq[(DenseVector[Double], DenseVector[Double], DenseMatrix[Double])] = {
    val lineCount = bathLines.cols / 2
    for (i <- 0 until lineCount) yield distPro(bathLines(::, 2 * i), bathLines(::, 2 * i + 1), data)
  }

  /**
    * 计算数据点到线的平方投影距离
    *
    * @param strPoLine 起点
    * @param endPoLine 终点
    * @param data      数据
    */
  def distPro(strPoLine: DenseVector[Double], endPoLine: DenseVector[Double]
              , data: DenseMatrix[Double])
  : Tuple3[DenseVector[Double], DenseVector[Double], DenseMatrix[Double]] = {
    //将线的点转成向量表示法
    val lineVector: DenseVector[Double] = endPoLine - strPoLine
    //获得线长度
    val lineLen: Double = norm(lineVector)
    val lineNormVector: DenseVector[Double] = lineVector :/ lineLen
    //沿线的单位向量
    val stMat = VectorUtil.vectorRep(strPoLine, data.cols, Orie.Horz)
    val diff = data - stMat
    //投影长度
    var indexPro: DenseVector[Double] = diff.t * lineNormVector
    //投影长度必须大于o小于线的长度
    indexPro = max(indexPro, 0.0)
    indexPro = min(indexPro, lineLen)
    //获得投影点坐标向量
    val pointPro: DenseMatrix[Double] = stMat.t + indexPro * lineNormVector.t
    //获取数据点到投影点的向量
    val vecPro: DenseMatrix[Double] = data.t - pointPro
    //求数据点到投影点的长度值
    val vecProLenSqu = vecPro :* vecPro //行 列
    (MatrixUtil.matrixSum(vecProLenSqu, Orie.Horz), indexPro, pointPro)
  }

  /**
    * 读取数据
    *
    * @param csvPath
    * @param propCount
    * @return
    */
  def csv2Mat(csvPath: String, propCount: Int): DenseMatrix[Double] = {
    val data = Source.fromFile(csvPath).getLines().toArray
    val lineCount = data.length
    val dataMat = DenseMatrix.zeros[Double](lineCount, propCount)
    var i = 0
    data.foreach(v => {
      val str = v.split(",")
      dataMat(i, ::) := DenseVector(str(0).toDouble, str(1).toDouble).t
      i += 1
    })
    dataMat
  }

  /**
    * 将矩阵保持为csv文件
    * 一行为一个点
    *
    * @param csvPath
    * @param lines
    */
  def mat2csv(csvPath: String, lines: DenseMatrix[Double]) = {
    val f = new File(csvPath)
    val fw = new FileWriter(f)
    val bw = new BufferedWriter(fw)
    bw.write(MatrixUtil.matrix2String(lines.t))
    bw.close()
    fw.close()
  }

  /**
    * 构建哈密顿路径连接方式
    *
    * @param lines 线段
    * @return 连接方式
    */
  def linkLines(lines: DenseMatrix[Double]) = {
    val lineCount = lines.cols / 2
    var linkArr = new ArrayBuffer[DenseVector[Int]]()
    if (lineCount != 1) {
      //当做TSP问题,计算各个点链接的代
      val tmp = cmpJoinCost(lines)
      val adjoinLinesArc = tmp._1
      val adjoinLinesLen = tmp._2
      adjoinLinesArc :*= ConfigPC.lamba
      val linkCost = adjoinLinesArc + adjoinLinesLen //代价
      val maxPlus = max(linkCost) + 1
      linkCost.foreachPair((index, value) => {
        if (value == 0) linkCost(index._1, index._2) = maxPlus
      })
      //经过性能比较，在求前75个最小值使用循环argmin比argtopk性能更高
      //对于argtopk，时间几乎不会变化
      var currLine = 0
      while (currLine < lineCount - 1) {
        val minIndex = argmin(linkCost) //找到最小值索引
        var (te1, te2) = minIndex._2 % 4 match {
            //连接的点 0 1 0 1
            case 0 => (minIndex._1 * 2, minIndex._2 / 4 * 2)
            case 1 => (minIndex._1 * 2, minIndex._2 / 4 * 2 + 1)
            case 2 => (minIndex._1 * 2 + 1, minIndex._2 / 4 * 2)
            case 3 => (minIndex._1 * 2 + 1, minIndex._2 / 4 * 2 + 1)
            case _ => throw new RuntimeException("not found min cost index!!!")
          }
        //做个排序，方便后面的计算
        if (te1 < te2) {
          te1 += te2; //把两数之和存到a中
          te2 = te1 - te2; //用两数和减去b可得原a,存储到b中
          te1 = te1 - te2; //因为b现在是原a值，所以，用两数和减去b(原a)可得 原b,存储到a中
        }
        var newLink = if (te1 % 2 == 0 && te2 % 2 == 0) {
          DenseVector[Int](te1 + 1, te1, te2, te2 + 1)
        } else if (te1 % 2 == 0 && te2 % 2 != 0) {
          DenseVector[Int](te1 + 1, te1, te2, te2 - 1)
        } else if (te1 % 2 != 0 && te2 % 2 == 0) {
          DenseVector[Int](te1 - 1, te1, te2, te2 + 1)
        } else {
          DenseVector[Int](te1 - 1, te1, te2, te2 - 1)
        }
        //判断点是否能插入
        //                var ter1 = IndexedSeq.empty[Int]
        //                var ter2 = IndexedSeq.empty[Int]
        var bbreak = false
        var goon = true
        //两个点在一条线内
        for (i <- 0 until linkArr.length if goon && !bbreak) {
          val tmp = linkArr(i)
          val ter1 = tmp.findAll(_ == te1)
          val ter2 = tmp.findAll(_ == te2)
          if (!ter1.isEmpty && !ter2.isEmpty) {
            if (math.abs(ter1(0) - ter2(0)) != 1) {
              goon = false
            }
            bbreak = true
          }
        }
        if (goon) {
          //两个点在两条线上，必须为端点
          bbreak = false
          for (i <- 0 until linkArr.length if !bbreak) {
            val tmp = linkArr(i)
            val ter1 = tmp.findAll(_ == te1)
            if (!ter1.isEmpty) {
              if (ter1(0) != tmp.length - 1 && ter1(0) != 0) {
                goon = false
              }
              bbreak = true
            }
          }
          if (goon) {
            bbreak = false
            for (i <- 0 until linkArr.length if !bbreak) {
              val tmp = linkArr(i)
              val ter2 = tmp.findAll(_ == te2)
              if (!ter2.isEmpty) {
                if (ter2(0) != tmp.length - 1 && ter2(0) != 0) {
                  goon = false
                }
                bbreak = true
              }
            }
          }
        }
        if (goon) {
          val linkArrBak = linkArr.clone()
          newLink = cirLink(linkArrBak, newLink, 0)


          //          var linkExc = new ArrayBuffer[Int]()
          //          //                    var linkLen = newLink.length
          //          for (i <- 0 until linkArr.length) {
          //            val lastLinkLen = newLink.length
          //            newLink = link(linkArr(i), newLink)
          //            if (newLink.length > lastLinkLen) {
          //              linkExc.append(i)
          //            }
          //          }
          //不是回路
          if (newLink(0) != newLink(newLink.length - 1) &&
            newLink(0) != newLink(newLink.length - 2)) {
            //            linkExc = linkExc.sortWith((v1, v2) => v1 >= v2)
            //            linkExc.foreach(k => linkArr.remove(k))
            linkArrBak.append(newLink)
            linkArr = linkArrBak
            currLine += 1
          }
        }
        linkCost(minIndex._1, minIndex._2) = maxPlus
      }
      if (linkArr.length != 1) {
        throw new RuntimeException("hp has failed!!!")
      } else {
        linkArr(0)
      }
    } else {
      DenseVector[Int](0, 1)
    }
  }

  /**
    * 将符合条件的连接连段连起来
    *
    * @param linkArr 已连接的连段
    * @param newLink 线加入的线段 对该线段和以前的连接
    * @param start   从以前的那条线段开始比较
    * @return
    */
  def cirLink(linkArr: ArrayBuffer[DenseVector[Int]], newLink: DenseVector[Int], start: Int): DenseVector[Int] = {
    if (!linkArr.isEmpty && start < linkArr.length) {
      val linkAgain = link(linkArr(start), newLink)
      if (linkAgain.length != newLink.length) {
        linkArr.remove(start)
        cirLink(linkArr, linkAgain, 0)
      } else {
        cirLink(linkArr, linkAgain, start + 1)
      }
    } else {
      newLink
    }
  }

  /**
    * 按顺序连接连接点
    *
    * @param oldLink
    * @param newLink
    * @return
    */
  def link(oldLink: DenseVector[Int], newLink: DenseVector[Int]) = {
    val f = find(oldLink, newLink) //(2,3,-1,-1)
    val found = f.findAll(_ != -1) //(0,1)
    val findLen = found.length
    var linkBak = newLink.copy
    if (findLen >= 2) {
      val nw1 = newLink(found(0))
      val nw2 = newLink(found(findLen - 1))
      val mm = minMax(f(found(0) to found(findLen - 1)))
      val od1 = oldLink(mm._1)
      val od2 = oldLink(mm._2)
      if (od1 == nw1 && od2 == nw2) {
        if (found(0) == 0) {
          linkBak = DenseVector.vertcat(oldLink, linkBak(findLen to -1))
        } else {
          linkBak = DenseVector.vertcat(linkBak(0 to linkBak.length - findLen - 1), oldLink)
        }
      } else if (od1 == nw2 && od1 == nw2) {
        linkBak = reverse(linkBak)
        if (found(0) == 0) {
          linkBak = DenseVector.vertcat(linkBak(0 to linkBak.length - findLen - 1), oldLink)
        } else {
          linkBak = DenseVector.vertcat(oldLink, linkBak(findLen to -1))
        }
      }
    }
    linkBak
  }

  /**
    * 查找
    *
    * @param oldLink
    * @param newLink
    * @return
    */
  def find(oldLink: DenseVector[Int], newLink: DenseVector[Int]) = {
    val re = DenseVector.zeros[Int](newLink.length)
    newLink.foreachPair((k, v) => {
      val f = oldLink.findAll(_ == v)
      if (f.isEmpty) {
        re(k) = -1
      } else {
        re(k) = f(0)
      }
    })
    re
  }

  /**
    * 求两条线连接连接的距离
    *
    * @param strPoLine1
    * @param endPoLine1
    * @param strPoLine2
    * @param endPoLine2
    * @return
    */
  def edgeLen(strPoLine1: DenseVector[Double], endPoLine1: DenseVector[Double],
              strPoLine2: DenseVector[Double], endPoLine2: DenseVector[Double]) = {
    //0-1 0-1
    //0 -0
    val l0 = VectorUtil.edPowDistance(strPoLine1, strPoLine2)
    //0 -1
    val l1 = VectorUtil.edPowDistance(strPoLine1, endPoLine2)
    //1-0
    val l2 = VectorUtil.edPowDistance(endPoLine1, strPoLine2)
    //1-1
    val l3 = VectorUtil.edPowDistance(endPoLine1, endPoLine2)

    DenseVector(l0, l1, l2, l3)
  }

  /**
    * 求两条线断点连接形成的夹角
    * 假设这两条线的端点编号为：0 1 2 3，
    * 这求得夹角为0-2 0-3 1-2 1-3
    * 夹角大小为沿着逆时针的方向
    *
    * @param strPoLine1
    * @param endPoLine1
    * @param strPoLine2
    * @param endPoLine2
    * @return
    */
  def edgeAr(strPoLine1: DenseVector[Double], endPoLine1: DenseVector[Double],
             strPoLine2: DenseVector[Double], endPoLine2: DenseVector[Double]) = {
    //0-1 0-1
    //0 -0
    val a00 = VectorUtil.arc(strPoLine1 - endPoLine1, strPoLine2 - strPoLine1) +
      VectorUtil.arc(strPoLine2 - strPoLine1, endPoLine2 - strPoLine2)
    //0-1
    val a01 = VectorUtil.arc(strPoLine1 - endPoLine1, endPoLine2 - strPoLine1) +
      VectorUtil.arc(endPoLine2 - strPoLine1, strPoLine2 - endPoLine2)
    //1-0
    val a10 = VectorUtil.arc(endPoLine1 - strPoLine1, strPoLine2 - endPoLine1) +
      VectorUtil.arc(strPoLine2 - endPoLine1, endPoLine2 - strPoLine2)
    //1-1
    val a11 = VectorUtil.arc(endPoLine1 - strPoLine1, endPoLine2 - endPoLine1) +
      VectorUtil.arc(endPoLine2 - endPoLine1, strPoLine2 - endPoLine2)

    DenseVector[Double](a00, a01, a10, a11)
  }

  /**
    * 将点转成投影长度
    *
    * @param lines
    * @param link
    * @param data
    * @return
    */
  def map2Acrl(lines: DenseMatrix[Double], link: DenseVector[Int],
               data: DenseMatrix[Double]) = {
    val (newLines, newLinesLen) = getIndexedSeg(lines, link)
    val allProDis = DenseMatrix.zeros[Double](data.rows, newLines.cols / 2)
    val rest = new ArrayBuffer[DenseMatrix[Double]]()
    for (i <- 0 until newLines.cols / 2) {
      val (proDis, proInd, proPot) = PCUtil.distPro(newLines(::, i * 2), newLines(::, i * 2 + 1), data.t)
      allProDis(::, i) := proDis
      rest.append(DenseMatrix.horzcat(proInd.asDenseMatrix.t, proPot))
    }
    val tmp = MatrixUtil.mmMatrixMin(allProDis, Orie.Horz)
    val minValue = tmp._1
    val minIndex = tmp._2
    val y = DenseMatrix.zeros[Double](data.rows, 3)
    for (i <- 0 until data.rows) {
      val ttIndex = minIndex(i)
      //      val tmp: DenseMatrix[Double] = rest(minIndex(i))
      y(i, ::) := rest(minIndex(i))(i, ::) //
      y(i, 0) += newLinesLen(minIndex(i))
    }
    //    (minValue, y, newLines)
    (minValue, y, lines)
  }

  /**
    * 通过已给的信息求线段长度及线段
    *
    * @param lines
    * @param link
    */
  def getIndexedSeg(lines: DenseMatrix[Double], link: DenseVector[Int]) = {
    val lineCount = lines.cols / 2
    if (lineCount != 1) {
      val newLines = DenseMatrix.zeros[Double](2, (lines.cols - 1) * 2)
      val newLinesLen = DenseVector.zeros[Double](lines.cols - 1)
      var i = 0
      var j = 0
      for (k <- 0 until link.length - 1) {
        newLines(::, i) := lines(::, link(k))
        newLines(::, i + 1) := lines(::, link(k + 1))
        if (j == 0) {
          newLinesLen(j) = norm(newLines(::, i) - newLines(::, i + 1))
        } else {
          newLinesLen(j) = newLinesLen(j - 1) +
            norm(newLines(::, i) - newLines(::, i + 1))
        }
        i += 2
        j += 1
      }
      (newLines, newLinesLen)
    }
    else {
      (lines, DenseVector[Double](0.0))
    }
  }

  /**
    * 求线连接的代价
    * 每一条线与另外一条线有四个连接代价
    * m的存储方法如下：
    * 每个数字代表线的编号
    * 每个对应关系产生四个代价
    * 分别是四个不同的连接方式
    * m是一个对称矩阵，每根线的连接代价占用四个元素
    * 0-0 0-1 0-2 0-3 0-4
    * 1-0 1-1 1-2 1-3 1-4
    * 2-0 2-1 2-2 2-3 2-4
    * 3-0 3-1 3-2 3-3 3-4
    * 4-0 4-1 4-2 4-3 4-4
    * 0-2 0-3 1-2 1-3
    *
    * @param lines 线段矩阵
    * @return
    */
  private def cmpJoinCost(lines: DenseMatrix[Double]) = {
    val lineCount = lines.cols / 2
    val adjoinLinesLen = DenseMatrix.zeros[Double](lineCount, 4 * lineCount)
    val adjoinLinesArc = adjoinLinesLen.copy
    for (i <- 0 until lineCount - 1) {
      for (j <- (i + 1) until lineCount) {
        val arc = edgeAr(lines(::, 2 * i), lines(::, 2 * i + 1), lines(::, 2 * j), lines(::, 2 * j + 1))
        val len = edgeLen(lines(::, 2 * i), lines(::, 2 * i + 1), lines(::, 2 * j), lines(::, 2 * j + 1))
        adjoinLinesArc(i, j * 4 to (j + 1) * 4 - 1) := arc.t
        adjoinLinesLen(i, j * 4 to (j + 1) * 4 - 1) := len.t
      }
    }
    (adjoinLinesArc, adjoinLinesLen)
  }

  /**
    * 去掉最后线段中的平行线段
    * 当做过拟合处理
    * 思想：
    * 当线段的起点 重点 断电 到另一条线的距离相差不大
    * 且距离为[-1.5sigma,1.5sigma],则合并这两条线，取中心线段
    * 长度为长线段的长度
    *
    * @param lines
    */
  def filterParallelLines(lines: DenseMatrix[Double]) = {
    lines
  }

  /**
    * 合并多个类聚类完成后，分类临界位置
    * 提取到的局部主成分线的点
    * @param lines
    * @return
    */
  def mergeCriticalPoint(lines: DenseMatrix[Double]) = {
    lines
  }
}
