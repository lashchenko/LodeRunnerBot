package com.blogspot.lashchenko.loderunner.user

import java.io.File
import java.net.URI
import java.util.concurrent.atomic.AtomicLong

import akka.actor.ActorSystem
import com.blogspot.lashchenko.loderunner.util.{MappingAction, Util}
import io.backchat.hookup._

import scala.swing._
import scala.swing.event._


object ApplicationMainUser extends SimpleSwingApplication {

  @volatile var action = 'Q'
  val counter = new AtomicLong(0)

  def top = new MainFrame {

    title = "LodeRunner"
    contents = new Button {

      text = "move A/S/D/W action Q/E"

      listenTo(keys)

      reactions += {
        case KeyPressed(_, key,_,_) =>
          val symbol = key.toString.head
          println(s"pressed: $symbol")
          text = s"pressed: $symbol"
          if (MappingAction.mapping.keySet.contains(symbol)) {
            ApplicationMainUser.action = symbol
          }
          if (key == Key.Escape) dispose()
      }
    }
  }

  val printer = new java.io.PrintWriter(new File(s"train-${System.currentTimeMillis()}.txt"))

//  @volatile var start = 0L
  
  val system = ActorSystem("LodeRunnerSystem")

  val client = new HookupClient {
    val user = "xyz"
    val server = "ws://tetrisj.jvmhost.net:12270/codenjoy-contest/ws?user="
    val uri = URI.create(server + user)
    val settings = HookupClientConfig(uri)

    var heroAction = "RIGHT"

    def receive = {
      case TextMessage(response) =>
//        println(response)
//        val step = counter.incrementAndGet()
        val board = Util.getBoardFeatures(response, 33)
        try {
          heroAction = MappingAction.mapping(action)
        } catch {
          case t: Throwable => println(t.getMessage)
        } finally {
          send(heroAction)
//          println(s"${Mapping.encode(action)},$border")
//          println(s"step $step : $heroAction -> ...")
//          val local = Util.getBoardFeatures(response, 3).split(',').map(Util.mkChar)
          val local = Util.getBoardFeatures(response, 3).feature.split(',').map(Util.mkChar)
          val isAvailable = Util.isAvailable(MappingAction.encode(action), local)
          if (isAvailable) {
            printer.println(s"${MappingAction.encode(action)},${board.feature}")
          }
        }
    }

    connect() onSuccess {
      case _ => println(s"connected to: ${uri.toASCIIString}")
    }
  }


}