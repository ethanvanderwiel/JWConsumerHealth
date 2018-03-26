package com.banno.template.metrics


import cats.effect._
import com.codahale.metrics._
import org.http4s.server.metrics._
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

object Metrics {

  def service[F[_]](metricRegistry: MetricRegistry)(implicit F: Sync[F]): HttpService[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    HttpService[F] {
      case GET -> Root / "metrics" =>
        metricsResponse(metricRegistry)
    }
  }

}
