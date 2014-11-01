package com.blogspot.lashchenko.loderunner.bot

import java.net.URI
import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import com.blogspot.lashchenko.loderunner.util.{MappingAction, Util}
import io.backchat.hookup._

import scala.util.Random


object ApplicationMainBot extends App {

  val counter = new AtomicLong(0)

  @volatile var start = 0L

  val system = ActorSystem("LodeRunnerSystem")

  val client = new HookupClient {
    val user = "xyz"
    val server = "ws://tetrisj.jvmhost.net:12270/codenjoy-contest/ws?user="
    val uri = URI.create(server + user)
    val settings = HookupClientConfig(uri)

    var heroAction = "RIGHT"

    def receive = {
      case TextMessage(response) =>
        val step = counter.incrementAndGet()
        val start = System.currentTimeMillis()
        val board = Util.getBoardFeatures(response, 33)
        try {
          var result: Option[Int] = None

          // try to solve action manually
          result = Util.predictManual(board)
          println(s"manual result: $result")

          // ... and if we can not resolve action in current position we try to use clustering ...
          if (result.isEmpty) {
            import scala.sys.process._
            val pyBotPath = "/Users/alashchenko/Development/Workspace/LodeRunnerBot/PyBot"
            result = Some(Math.round(Seq(s"$pyBotPath/script.py", board.feature).!))
            //          val result = Math.round(Seq(s"$pyBotPath/script.py", board.feature).!)
            //          val result = Math.round(Seq(s"$pyBotPath/neural.py", board.feature).!)
//            println(s"$result -> Dec: ${MappingAction.decode(result.get)}")

            //          val local = Util.getBoardFeatures(response, 3).split(',').map(Util.mkChar)
            val local = Util.getBoardFeatures(response, 3).feature.split(',').map(Util.mkChar)
            val isAvailable = Util.isAvailable(result.get, local)

            // ... and if clustering result is mess just generate random action
            if (!isAvailable) {
//              var m = MappingAction.mapping.map(_).toMap
//              var ixs = (0 until MappingAction.mapping.size).toSet

              println(s"clustering ($result) action ($heroAction) is unavailable so just generate any random action")
              //            heroAction = MappingAction.mapping(MappingAction.decode(Random.nextInt(MappingAction.mapping.size)))
              result = Random.shuffle((0 to 5).toList).find(a => Util.isAvailable(a, local)) match {
                case Some(k) => Some(k)
                case _ => Some(Random.nextInt(MappingAction.mapping.size))
              }
            }
          }



//          val positive = if (isAvailable) { positiveCounter.incrementAndGet() } else { positiveCounter.get() }
//          println(s"positive: $positive")

//          val negative = if (!isAvailable) { negativeCounter.incrementAndGet() } else { negativeCounter.get() }
//          println(s"negative: $negative")



          heroAction = MappingAction.mapping(MappingAction.decode(result.get))
        } catch {
          case t: Throwable =>
            println(t.getMessage)
            t.printStackTrace()
        } finally {
          send(heroAction)
          println(s"step $step : $heroAction -> ... time: ${System.currentTimeMillis()-start}")
        }
    }

    connect() onSuccess {
      case _ => println(s"connected to: ${uri.toASCIIString}")
    }
  }

  val positiveCounter = new AtomicLong(1)
  val negativeCounter = new AtomicLong(1)
}