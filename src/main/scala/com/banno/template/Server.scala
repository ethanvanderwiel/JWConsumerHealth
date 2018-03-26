package com.banno.template

import fs2._
import cats.effect.Effect
import cats.implicits._
import com.banno.template.admin.Admin
import com.banno.template.config.ConfigService
import com.banno.template.health.Health
import com.banno.template.metrics.Metrics
import com.codahale.metrics.MetricRegistry
import org.http4s.server.metrics.{Metrics => MetricsMiddleware}

import scala.concurrent.ExecutionContext

object Server {

  private val logger = org.log4s.getLogger

  def serve[F[_]](implicit Effect: Effect[F], EC: ExecutionContext): Stream[F, StreamApp.ExitCode] =
    for {
      Scheduler <- Scheduler(10)
      configService <- ConfigService.impl[F](Effect/*, Scheduler, EC*/)
      _ = configService.httpClient // TODO: Use HttpClient
      _ <- configService.serviceDiscovery // TODO: Use Service Discovery
      // _ <- configService.transactor // TODO: Use Transactor
      // _ <- Stream.eval(configService.runMigrations) // TODO: Uncomment When You Have Migrations

      // Mutable State Ball - Use with care
      metricRegistry = new MetricRegistry()
      metricsService = Metrics.service[F](metricRegistry)

      healthService = MetricsMiddleware(metricRegistry, "com.banno.template.health")(Effect)(Health.service[F])
      adminService = Admin.service[F]


      _ <- configService.registerInstance
      exitCode <- Stream(
        configService.primaryHttpServer
          .mountService(healthService, "/")
          .serve,
        configService.administrativeHttpServer
          .mountService(healthService <+> metricsService <+> adminService, "/")
          .serve
      )
        .join(2)
        .concurrently(
          Stream.eval(Effect.delay(logger.info("template-service has started")))
        )

    } yield exitCode

}
