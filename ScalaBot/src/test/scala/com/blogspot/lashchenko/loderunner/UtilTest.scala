package com.blogspot.lashchenko.loderunner

import java.util.concurrent.atomic.AtomicLong

import com.blogspot.lashchenko.loderunner.util.Util
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class UtilTest extends FlatSpec with Matchers {

  "Util.getBorder" should "extract horizontal and vertical path" in {
    val fileName = "test-train"
    val cnt = new AtomicLong()
//    for (line <- Source.fromFile(s"$fileName.txt").getLines() if cnt.getAndIncrement < 10) {
    for (line <- Source.fromURL(getClass.getResource(s"/$fileName.txt")).getLines() if cnt.getAndIncrement < 10) {
      val example = line

      val act = example.charAt(0)
      val arr = example.split(',').tail
      val text = arr.map(Util.mkChar).mkString("")
      val response = s"board=$text"

      val board = Util.getBoardFeatures(response, 11)
      Util.printBoard(text)
      val res = Util.predictManual(board)
      println(res)
    }
    true should be (java.lang.Boolean.TRUE)
  }

}
