package com.banno.template.admin
import java.lang.management.ManagementFactory

import cats.effect.Sync
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

object Admin {

  def service[F[_]](implicit F: Sync[F]): HttpService[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpService[F] {
      case _ -> Root / "threads" =>
        Ok(
          ManagementFactory.getThreadMXBean
            .dumpAllThreads(true, true)
            .map(_.toString)
            .mkString("\n")
        )
    }
  }

}
