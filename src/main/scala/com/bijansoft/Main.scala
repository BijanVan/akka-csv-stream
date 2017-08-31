package com.bijansoft

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val theSystem: ActorSystem = ActorSystem()
//  implicit val materializer     = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = theSystem.dispatcher

  val config       = ConfigFactory.load()
  val csvProcessor = new CsvProcessor()
  val server       = new Server(csvProcessor.liveReadings)
  server.startServer(config.getString("server.host"),
                     config.getInt("server.port"),
                     ServerSettings(ConfigFactory.load),
                     theSystem)
}
