package com.bijansoft

import akka.NotUsed
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.scaladsl.{Flow, Sink, Source}

class Server(private val readings: Source[Double, NotUsed]) extends HttpApp {

  override def routes: Route =
    pathSingleSlash {
      getFromResource("index.html")
    } ~
      path("data") {
        val messages = readings.map(v => TextMessage(v.toString))
        handleWebSocketMessages(Flow.fromSinkAndSourceCoupled(Sink.ignore, messages))
      } ~
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }
}
