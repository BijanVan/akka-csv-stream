package com.bijansoft

import java.nio.file.{Path, Paths}
import java.util.logging.Logger

import akka.NotUsed
import akka.event.Logging
import akka.stream.{Attributes, ClosedShape, FlowShape, ThrottleMode}
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.file.DirectoryChange
import akka.stream.alpakka.file.scaladsl.DirectoryChangesSource
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, Sink}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future, duration}
import scala.concurrent.duration.FiniteDuration

class CsvProcessor(implicit e: ExecutionContext) {
  import CsvProcessor._

  private val newFiles = DirectoryChangesSource(dataDir, pollInterval, 128)

  private val csvPaths = Flow[(Path, DirectoryChange)]
    .filter(t => isCsvFileCreationEvent(t._1, t._2))
    .log("New file", _._1.getFileName)
    .map(_._1)

  private val fileBytes = Flow[Path].flatMapConcat(FileIO.fromPath(_))

  private val csvFields = Flow[ByteString]
    .via(CsvParsing.lineScanner())
    .throttle(100, FiniteDuration(1, duration.SECONDS), 10, ThrottleMode.shaping)

  private val readings = Flow[Seq[ByteString]].map(Reading(_))

  private val averageReadings = Flow[Reading]
    .grouped(2)
    .mapAsyncUnordered(10)(readings => Future((readings.head.value + readings.tail.head.value) / 2.0))
    .filter(_ > averageThreshold)
    .log("Greater than", _ => averageThreshold)

  private val notifier = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val mailerSink = Flow[Double].grouped(emailThreshold).to(Sink.foreach(_ => logger.info("Sending e-mail")))
    val mailer = builder.add(mailerSink)
    val bcast = builder.add(Broadcast[Double](2))
    bcast.out(1) ~> mailer

    FlowShape.of(bcast.in, bcast.out(0))
  }

  private[bijansoft] val liveReadings = newFiles
    .via(csvPaths)
    .via(fileBytes)
    .via(csvFields)
    .via(readings)
    .via(averageReadings)
    .via(notifier)
    .withAttributes(
      Attributes.logLevels(onElement = Logging.InfoLevel, onFinish = Logging.InfoLevel, onFailure = Logging.InfoLevel))

  private def isCsvFileCreationEvent(path: Path, directoryChange: DirectoryChange): Boolean = {
    path.toString.endsWith(".csv") && directoryChange == DirectoryChange.Creation
  }
}

object CsvProcessor {
  private val config  = ConfigFactory.load()
  private val dataDir = Paths.get(config.getString("csv-processor.data-dir"))
  private val pollInterval =
    FiniteDuration(config.getDuration("csv-processor.data-dir-poll-interval").toNanos, duration.NANOSECONDS)
  private val averageThreshold = config.getDouble("csv-processor.average-threshold")
  private val emailThreshold   = config.getInt("csv-processor.email-threshold")
  private val logger           = Logger.getLogger("CsvProcessor")
}
