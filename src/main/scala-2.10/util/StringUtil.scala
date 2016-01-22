package util

/**
  * Created by hy on 15-12-7.
  */
object StringUtil {

  /**
    * 判断字符串是否是数字
    *
    * @param str
    * @return
    */
  def isNumeric(str: String): Boolean = {
    str.matches("[-+]?\\d+(\\.\\d+)?")
  }
}
