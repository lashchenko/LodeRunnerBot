package com.blogspot.lashchenko.loderunner.util

import java.io.File

import scala.io.Source

object Remover extends App {

//  val fileName = "train-1414540061974"
//  val fileName = "train-1414541251995"
  val fileName = "train-old"
  val printer = new java.io.PrintWriter(new File(s"filtered-$fileName.txt"))

  for (line <- Source.fromFile(s"$fileName.txt").getLines()) {

    val example = line

    val act = example.charAt(0)
    val arr = example.split(',').tail

    val board = "board="+arr.map(Util.mkChar).mkString("")

    // 0 1 2
    // 3 4 5
    // 6 7 8
    val local = Util.getBoardFeatures(board, 3).feature.split(',').map(Util.mkChar)

    // check is action available ...
    //  0 Q 81 (LEFT,ACT)
    //  1 E 69 (RIGHT,ACT)
    //  2 W 87 UP
    //  3 S 83 DOWN
    //  4 A 65 LEFT
    //  5 D 68 RIGHT
    val isAvailable = Util.isAvailable(act.toString.toInt, local)

    if (isAvailable) {
      printer.println(line)
    }
//    else {
//      try {
//        val n = 33
//        (0 to n - 1).foreach { i =>
//          (0 to n - 1).foreach { j =>
//            print(arr(i * n + j).toInt.toChar)
//          }
//          println()
//        }
//        println("------------")
//        (0 to 2).foreach { i =>
//          (0 to 2).foreach { j =>
//            print(local(i * 3 + j))
//          }
//          println()
//        }
//        val int = java.lang.Integer.valueOf(act.toString).toInt
//        println(s"ACT ($act / $int) : ${MappingAction.mapping(MappingAction.decode(int))}")
//      } catch {
//        case t: Throwable => println(t.getMessage)
//      }
//    }

  }



}



