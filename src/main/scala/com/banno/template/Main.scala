package com.banno.jabberwocky.consumer.health

import cats.effect.IO
import fs2.StreamApp
import fs2.Stream
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends StreamApp[IO] {
  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    Server.serve[IO]
}
