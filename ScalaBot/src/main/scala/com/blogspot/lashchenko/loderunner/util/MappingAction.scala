package com.blogspot.lashchenko.loderunner.util

object MappingAction {

  //  0 Q 81 (LEFT,ACT)
  //  1 E 69 (RIGHT,ACT)
  //  2 W 87 UP
  //  3 S 83 DOWN
  //  4 A 65 LEFT
  //  5 D 68 RIGHT

  val LeftAct = 0
  val RightAct = 1
  val Up = 2
  val Down = 3
  val Left = 4
  val Right = 5


  val mapping = Map(
    'Q' -> "(LEFT,ACT)",
    'E' -> "(RIGHT,ACT)",
    'W' -> "UP",
    'S' -> "DOWN",
    'A' -> "LEFT",
    'D' -> "RIGHT"
  )

  def decode(i: Int) = i match {
    case 0 => 'Q'
    case 1 => 'E'
    case 2 => 'W'
    case 3 => 'S'
    case 4 => 'A'
    case _ => 'D'
  }

  def encode(c: Char) = c match {
    case 'Q' => 0
    case 'E' => 1
    case 'W' => 2
    case 'S' => 3
    case 'A' => 4
    case _ => 5
  }
}
