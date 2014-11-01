package com.blogspot.lashchenko.loderunner.util

import scala.collection.mutable.ListBuffer

//object Util extends App { // App for debug
object Util { // App for debug

//  case class Board(matrix: Array[Array[Int]], feature: String, horizontal: Array[Char], vertical: Array[Char])
  case class Board(matrix: Array[Array[Int]], feature: String, text: String, position: Int)

  val N = 33
  val n = 3

  def mkChar(str: String) = {
    java.lang.Integer.valueOf(str).toChar
  }

  // it is not important so we can just replace any state to '0'
  val HERO_DIE = 'Ѡ'
  val HERO_DRILL_LEFT = 'Я'
  val HERO_DRILL_RIGHT = 'R'
  val HERO_LADDER = 'Y'
  val HERO_LEFT = '◄'
  val HERO_RIGHT = '►'
  val HERO_FALL_LEFT = ']'
  val HERO_FALL_RIGHT = '['
  val HERO_PIPE_LEFT = '{'
  val HERO_PIPE_RIGHT = '}'
  val HeroIcons = Set('Ѡ', 'Я', 'R', 'Y', '◄', '►', ']', '[', '{', '}')

  val EnemyIcons = Set('Q', '<', '>', 'X', '«', '»')

  val OtherIcons = Set('Z', ')', '(', 'U', 'Э', 'Є')

  val UndestroyableIcon = '☼'

  val DrillIcons = Set('.', '*', '4', '3', '2') // '1' will be closed in next step
  val PathIcons = Set(' ', 'H', '~') ++ DrillIcons

  val GoldIcon = '$'

  val BrickIcon = '#'


  val example2 = "☼☼☼☼☼☼☼☼☼☼ $      H ☼☼☼☼☼☼☼☼H☼        H ~~~~~~~~H  $      H         H   ►     H ☼☼☼☼☼☼☼☼☼☼~~~~~~~~~~"
  val example = "☼☼☼☼☼☼☼☼☼☼ $  Z   H ☼☼☼☼☼☼☼☼H☼        H ~~~~~~~~H  $      ►         H     X   H ☼☼☼☼☼☼☼☼☼☼~~~~~~~~~~"

  def printBoard(text: String): Unit = {
    val len = Math.sqrt(text.length).toInt
    (0 to text.length-1 by len).foreach { pos =>
          println(f"$pos%5d: ${text.substring(pos, pos + len)}")
    }
  }

  def getBoardFeatures(response: String, n: Int) = {
    val text = if (response.startsWith("board=")) response.substring(6) else response
    val len = Math.sqrt(text.length).toInt
//    println(s"Len: $len")

    var heroIndex = -1
    HeroIcons.find { symbol =>
      val i = text.indexOf(symbol)
      heroIndex = i
      i >= 0
    }

    val horizontalPos = heroIndex % len
    val verticalPos = heroIndex / len
//    println(s"HeroIndex = $heroIndex")
//    println(s"HeroPos: $horizontalPos x $verticalPos")

    val N = n
    val matrix = Array.ofDim[Int](N, N)

    val vertical = (verticalPos-N/2) to (verticalPos+N/2)
    val horizontal = (horizontalPos - N / 2) to (horizontalPos + N / 2)

    vertical.foreach { y =>
      horizontal.foreach { x =>
        val p = y*len + x
        val symbol = if (Set(x,y).exists(t => t < 0 || t >= len) || p < 0 || p >= text.length)
          UndestroyableIcon
        else
          text.charAt(p)
        matrix(y - vertical.head)(x - horizontal.head) = symbol
      }
    }
//    matrix.foreach(arr => println(arr.map(_.toChar.toString).mkString))
//    println("extracted train set {action,features}:")
    val feature = matrix.map(arr => arr.mkString(",")).mkString(",")

    Board(matrix, feature, response, heroIndex)
  }

  var orderedAction = {
    val l = ListBuffer[(Array[Char], Array[Array[Char]], Array[Array[Char]]) => Option[Int]]()

    // 0 1 2
    // 3 4 5
    // 6 7 8

    l += ((local, hor, ver) => if (isAvailable(MappingAction.Left, local) && local(3) == '$') Some(MappingAction.Left) else None)
    l += ((local, hor, ver) => if (isAvailable(MappingAction.Left, local) && local(6) == '$') Some(MappingAction.Left) else None)

    l += ((local, hor, ver) => if (isAvailable(MappingAction.Right, local) && local(5) == '$') Some(MappingAction.Right) else None)
    l += ((local, hor, ver) => if (isAvailable(MappingAction.Right, local) && local(8) == '$') Some(MappingAction.Right) else None)

    l += ((local, hor, ver) => if (isAvailable(MappingAction.Up, local) && local(1) == '$' && local(4) == Util.HERO_LADDER) Some(MappingAction.Up) else None)

    l += ((local, hor, ver) => if (isAvailable(MappingAction.Down, local) && local(7) == '$') Some(MappingAction.Down) else None)

    l += ((local, hor, ver) => {
      val bottom = ver(1).slice(ver(1).length/2+1, ver(1).length).mkString("")
//      println(s"predict manual: bottom = $bottom")
      if (isAvailable(MappingAction.Down, local) && bottom.matches("[H~ 43*]{0,}\\$.*"))
        Some(MappingAction.Down)
      else
        None
    })

    l += ((local, hor, ver) => {
      val top = ver(1).slice(0, ver(1).length/2).mkString("")
      val verLeft = ver(0).slice(0, ver(0).length/2).mkString("")
      val verRight = ver(2).slice(0, ver(2).length/2).mkString("")
//      println(s"predict manual: top = $top")
      if (isAvailable(MappingAction.Up, local) && top.matches(".*[H]{1,}") && (
          top.matches(".*\\$[H]{1,}")
          || verLeft.matches(".*\\$[H #4321.☼]{1,}")
          || verRight.matches(".*\\$[H #4321.☼]{1,}"))
      )
        Some(MappingAction.Up)
      else
        None
    })

    l += ((local, hor, ver) => {
      val right = hor(1).slice(hor(1).length/2+1, hor(1).length).mkString("")
      val verRight = ver(2).slice(ver(2).length/2+1, ver(2).length).mkString("")
//      println(s"predict manual: right = $right")
      if (isAvailable(MappingAction.Left, local) && (
          verRight.matches("[#H~ *43]{0,}\\$.*")
          || right.matches("[H ]{0,}\\$.*")
          || right.matches("[~]{0,}\\$.*")
          || (right.matches("[ ]{0,}\\$.*") && local(8) == BrickIcon)
          || (right.matches("[ ]{0,}\\$.*") && local(8) == UndestroyableIcon)
          || (right.matches("[ ]{0,}\\$.*") && OtherIcons.contains(local(8)))
        ))
        Some(MappingAction.Right)
      else
        None
    })

    l += ((local, hor, ver) => {
      val bottom = ver(0).slice(ver(0).length/2+1, ver(0).length).mkString("")
      if (isAvailable(MappingAction.Left, local) && bottom.matches("[H~ *43]{0,}\\$.*"))
        Some(MappingAction.Left)
      else
        None
    })

    l += ((local, hor, ver) => {
      val bottom = ver(2).slice(ver(2).length/2+1, ver(2).length).mkString("")
      if (isAvailable(MappingAction.Right, local) && bottom.matches("[H~ *43]{0,}\\$.*"))
        Some(MappingAction.Right)
      else
        None
    })

    l += ((local, hor, ver) => {
      val left = hor(1).slice(0, hor(1).length/2).mkString("")
      val verLeft = ver(0).slice(ver(0).length/2+1, ver(0).length).mkString("")
//      println(s"predict manual: left = $left")
      if (isAvailable(MappingAction.Left, local) && (
          verLeft.matches("[#H~ *43]{0,}\\$.*")
          || left.matches(".*\\$[H ]{0,}")
          || left.matches(".*\\$[~]{0,}")
          || (left.matches(".*\\$[ ]{0,}") && local(6) == BrickIcon)
          || (left.matches(".*\\$[ ]{0,}") && local(6) == UndestroyableIcon)
          || (left.matches(".*\\$[ ]{0,}") && OtherIcons.contains(local(6)))
        ))
        Some(MappingAction.Left)
      else
        None
    })

    l += ((local, hor, ver) => {
//      val bottom = ver(0).slice(ver(0).length/2+1, ver(0).length).mkString("")
      val verLeft = ver(0).slice(ver(0).length/2+1, ver(0).length).mkString("")
//      if (isAvailable(MappingAction.LeftAct, local) && local(6) == '#' && bottom.matches("[H~ *43]{0,}\\$.*"))
      if (isAvailable(MappingAction.LeftAct, local) && local(6) == '#' && verLeft.matches("[#H~ *43\\$]{1,}.*"))
        Some(MappingAction.LeftAct)
      else
        None
    })

//    l += ((local, hor, ver) => {
//      val bottom = ver(2).slice(0, ver(2).length/2).mkString("")
//      if (isAvailable(MappingAction.RightAct, local) && local(4) == HERO_LADDER && local(8) == '#' && bottom.matches("[H~ *43]{0,}\\$.*"))
//        Some(MappingAction.RightAct)
//      else
//        None
//    })
//
//    l += ((local, hor, ver) => {
//      val bottom = ver(0).slice(0, ver(0).length/2).mkString("")
//      if (isAvailable(MappingAction.Up, local) && local(4) == HERO_LADDER && local(6) == '#' && bottom.matches("[H~ *43]{0,}\\$.*"))
//        Some(MappingAction.Up)
//      else
//        None
//    })

    l += ((local, hor, ver) => {
//      val bottom = ver(2).slice(ver(2).length/2+1, ver(2).length).mkString("")
      val verRight = ver(2).slice(ver(2).length/2+1, ver(2).length).mkString("")
      if (isAvailable(MappingAction.RightAct, local) && local(8) == '#' && verRight.matches("[#H~ *43\\$]{1,}.*"))
        Some(MappingAction.RightAct)
      else
        None
    })

    l
  }


  def predictManual(board: Board): Option[Int] = {

    val local = Util.getBoardFeatures(board.text, 3).feature.split(',').map(Util.mkChar)
    val matrix = board.matrix

    lazy val h = Array(
      (0 until matrix.length).map(i => matrix(matrix.length/2-1)(i).toChar).toArray,
      (0 until matrix.length).map(i => matrix(matrix.length/2)(i).toChar).toArray,
      (0 until matrix.length).map(i => matrix(matrix.length/2+1)(i).toChar).toArray)
    lazy val v = Array(
      matrix.map(line => line(line.length/2-1).toChar),
      matrix.map(line => line(line.length/2).toChar),
      matrix.map(line => line(line.length/2+1).toChar)
    )

    println("local for manual: ")
    Util.printBoard(local.mkString(""))

    println("Ver:")
    (0 until v.head.length).foreach { i =>
      println(" " + v(0)(i) + v(1)(i) + v(2)(i))
    }

    println("Hor:")
    println(h(0).mkString(""))
    println(h(1).mkString(""))
    println(h(2).mkString(""))

    orderedAction.find(f => f(local, h, v).isDefined) match {
      case Some(f) =>
        orderedAction -= f
        orderedAction ++= List(f) // TODO check ordering
        orderedAction = orderedAction.reverse
        f(local, h, v)
      case _ =>
        None
    }
  }

  def isAvailable(action: Int, local: Array[Char]) = {
    val res = action match {
      case MappingAction.Left if local(3) == '$' => true
      case MappingAction.Left if local(3) == '~' => true
      case MappingAction.Left if local(3) == 'H' => true
      case MappingAction.Left if local(3) == ' ' => true
      case MappingAction.Left if Util.DrillIcons.contains(local(3)) => true

      case MappingAction.Right if local(5) == '$' => true
      case MappingAction.Right if local(5) == '~' => true
      case MappingAction.Right if local(5) == 'H' => true
      case MappingAction.Right if local(5) == ' ' => true
      case MappingAction.Right if Util.DrillIcons.contains(local(5)) => true

      case MappingAction.Up if local(1) == 'H' && local(4) == Util.HERO_LADDER => true
      case MappingAction.Up if local(1) == ' ' && local(4) == Util.HERO_LADDER => true
      case MappingAction.Up if local(1) == '$' && local(4) == Util.HERO_LADDER => true

      case MappingAction.Down if local(7) == 'H' => true
      case MappingAction.Down if local(7) == ' ' => true
      case MappingAction.Down if local(7) == '$' => true
      case MappingAction.Down if local(7) == '~' => true

      case MappingAction.LeftAct if local(6) == '#' && local(3) == ' ' => true
      case MappingAction.LeftAct if local(6) == '#' && local(3) == '~' => true

      case MappingAction.RightAct if local(8) == '#' && local(5) == ' ' => true
      case MappingAction.RightAct if local(8) == '#' && local(5) == '~' => true

      case _ => false
    }

    val isDebugEnabled = false
    if (!res && isDebugEnabled) {
      try {
        println("------------ LOCAL AREA AND UNAVAILABLE ACTION -------")
        println(s"ACT = $action --> ${MappingAction.mapping(MappingAction.decode(action))}")
        (0 to 2).foreach { i =>
          (0 to 2).foreach { j =>
            print(local(i * 3 + j))
          }
          println()
        }
        println("------------------------------------------------------")
      } catch {
        case t: Throwable => println(t.getMessage)
      }
    }
    res
  }


}
